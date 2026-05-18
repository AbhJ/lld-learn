# URL Shortener - Variations

## Variation 1: Custom Domains
**Learning Value:** Teaches multi-tenant domain management, DNS integration, and branded URL routing.

### Additional Requirements
- Branded short URLs using customer's own domain
- DNS mapping configuration (CNAME records)
- SSL certificate management per custom domain
- Domain verification to prevent spoofing
- Fallback handling for misconfigured domains

### Design Changes
- Add `CustomDomain` class with domain, owner, and SSL status
- Add `DomainVerifier` using DNS TXT record validation
- Add `SSLManager` for certificate provisioning (Let's Encrypt integration)
- Add `DomainRouter` to resolve incoming requests to correct tenant
- Modify URL generation to use custom domain prefix

### Solution Approach
Customers register their custom domain and verify ownership by adding a DNS TXT record. The system validates the TXT record, then instructs the customer to point a CNAME to the shortener's servers. SSL certificates are automatically provisioned via Let's Encrypt (ACME protocol) once DNS is configured. Incoming requests are routed by Host header: the DomainRouter looks up which tenant owns the domain and resolves the short code within that tenant's namespace. Each tenant has isolated short codes (same code on different domains can point to different URLs). Fallback pages handle requests to domains with incomplete setup.

### Key Classes to Add
```java
public class CustomDomain {
    private String domain;
    private String ownerId;
    private DomainStatus status; // PENDING_VERIFICATION, ACTIVE, SSL_PENDING
    private SSLCertificate certificate;
    
    public boolean verify() { ... }
    public void provisionSSL() { ... }
}

public class DomainRouter {
    private Map<String, CustomDomain> domainRegistry;
    
    public String resolveTenant(String hostHeader) {
        CustomDomain domain = domainRegistry.get(hostHeader);
        if (domain == null || domain.getStatus() != ACTIVE) return handleFallback();
        return domain.getOwnerId();
    }
}

public class DomainVerifier {
    public boolean verifyOwnership(String domain, String expectedToken) {
        // Check DNS TXT record for domain contains expectedToken
    }
}
```

---

## Variation 2: Link Expiration with Redirect
**Learning Value:** Introduces TTL-based resource lifecycle, expiration handling, and graceful redirect chains.

### Additional Requirements
- Links expire after configurable duration or date
- Expired links redirect to a landing page (not 404)
- Countdown timer showing time remaining
- Option to extend expiration
- Grace period with warning page before full expiration

### Design Changes
- Add `ExpirationPolicy` with TTL or absolute expiry date
- Add `ExpiredLinkHandler` for custom redirect on expiration
- Add `GracePeriod` with warning page before final expiry
- Modify `resolve()` to check expiration before redirecting
- Add `ExpirationExtender` for one-click renewal

### Solution Approach
Each shortened URL can optionally have an expiration policy: either a TTL (e.g., 7 days from creation) or an absolute date. When a short URL is resolved, the system checks if it has expired. If within a grace period, it shows a warning page ("This link expires in 2 hours") with the option to proceed. If fully expired, it redirects to a configurable landing page (the creator can set a custom expired-link page). An API allows link creators to extend expiration before or after it expires. Batch cleanup removes expired URLs from the active lookup table (but retains metadata for analytics).

### Key Classes to Add
```java
public class ExpirationPolicy {
    private Instant expiresAt;
    private Duration gracePeriod;
    private String expiredRedirectUrl;
    
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isInGracePeriod() { ... }
    public void extend(Duration extension) { expiresAt = expiresAt.plus(extension); }
}

public class ExpiredLinkHandler {
    public String handleExpiredLink(URLMapping mapping) {
        ExpirationPolicy policy = mapping.getExpirationPolicy();
        if (policy.isInGracePeriod()) {
            return renderWarningPage(mapping);
        }
        return policy.getExpiredRedirectUrl();
    }
}

public class URLMapping {
    private String shortCode;
    private String longUrl;
    private ExpirationPolicy expirationPolicy; // nullable
    private Instant createdAt;
}
```

---

## Variation 3: A/B Testing Links
**Learning Value:** Practices traffic splitting, variant routing, and click-through analytics for experimentation.

### Additional Requirements
- Single short URL routes to different destinations by percentage
- Configurable traffic split (e.g., 70%/30%)
- Persistent assignment (same user always sees same variant)
- Analytics per variant (clicks, conversions)
- Ability to adjust split or declare winner

### Design Changes
- Add `ABTest` with variants and traffic allocation
- Add `VariantRouter` for consistent user-to-variant assignment
- Add `ABAnalytics` tracking metrics per variant
- Modify `resolve()` to route through AB test logic
- Add `TestManager` for creating, adjusting, and concluding tests

### Solution Approach
An A/B test link has multiple destination URLs with traffic allocation percentages. When resolved, the system determines which variant to show. For consistent assignment, it hashes the user identifier (cookie, IP, or user ID) to deterministically map to a variant (ensures the same user always sees the same destination). Analytics track clicks and downstream conversions per variant. The test creator can adjust allocations in real-time or "call" the test by routing 100% to the winner. Statistical significance is computed to help decide when a variant has clearly won.

### Key Classes to Add
```java
public class ABTest {
    private String shortCode;
    private List<Variant> variants;
    private boolean active;
    
    public String resolveForUser(String userId) {
        int bucket = Math.abs(userId.hashCode()) % 100;
        int cumulative = 0;
        for (Variant v : variants) {
            cumulative += v.getPercentage();
            if (bucket < cumulative) return v.getDestinationUrl();
        }
        return variants.get(0).getDestinationUrl();
    }
}

public class Variant {
    private String name;
    private String destinationUrl;
    private int percentage; // 0-100
    private long clicks;
    private long conversions;
}

public class TestManager {
    public ABTest createTest(String shortCode, List<Variant> variants) { ... }
    public void adjustAllocation(String shortCode, Map<String, Integer> newAllocations) { ... }
    public void declareWinner(String shortCode, String variantName) { ... }
}
```

---

## Variation 4: QR Code Generation
**Learning Value:** Explores trade-offs between encoding density and scannability in visual code generation.

### Additional Requirements
- Generate QR code for any short URL
- Customizable design (colors, logo embedding, corner style)
- Multiple format outputs (PNG, SVG, PDF)
- Dynamic QR codes (destination can change after printing)
- Scan analytics (time, location, device)

### Design Changes
- Add `QRCodeGenerator` with customization options
- Add `QRDesign` for visual customization parameters
- Add `QRFormat` enum (PNG, SVG, PDF) with renderers
- Link QR codes to short URLs for dynamic destination changes
- Add scan tracking integrated with URL analytics

### Solution Approach
QR codes encode the short URL (not the long URL), so the destination can be changed without reprinting. The generator creates a QR matrix using standard encoding (Reed-Solomon error correction), then applies visual customizations: brand colors for modules, rounded corners, and an embedded logo in the center (leveraging error correction to maintain scannability). Multiple output formats are supported via format-specific renderers. Since the QR points to the short URL, all scans route through the shortener, enabling full analytics (scan time, approximate location via IP, device type). Higher error correction levels (H/Q) allow more visual customization while remaining scannable.

### Key Classes to Add
```java
public class QRCodeGenerator {
    public byte[] generate(String shortUrl, QRDesign design, QRFormat format) {
        // 1. Encode URL into QR matrix
        // 2. Apply design customizations
        // 3. Render to requested format
    }
}

public class QRDesign {
    private Color foregroundColor;
    private Color backgroundColor;
    private byte[] logoImage; // nullable
    private CornerStyle cornerStyle; // SQUARE, ROUNDED, DOTS
    private ErrorCorrectionLevel ecLevel; // L, M, Q, H
}

public enum QRFormat {
    PNG, SVG, PDF
}

public class DynamicQR {
    private String qrId;
    private String shortCode; // Points to short URL (destination can change)
    private QRDesign design;
    private long scanCount;
}
```

---

## Variation 5: Link-in-Bio Page
**Learning Value:** Deepens understanding of page composition, link aggregation, and personalized landing page design.

### Additional Requirements
- Single page hosting multiple links for a user
- Customizable page theme and layout
- Analytics per link on the page
- Drag-and-drop link ordering
- Social media integration and profile display

### Design Changes
- Add `BioPage` class with user profile and ordered links
- Add `BioLink` for individual links on the page
- Add `PageTheme` for visual customization
- Add `BioPageRenderer` generating the HTML page
- Add per-link analytics (clicks, CTR, referrer)

### Solution Approach
A link-in-bio page is a single URL (e.g., short.ly/username) that renders a customizable page with multiple links. The page has a user profile section (avatar, name, bio) followed by an ordered list of links. Each link has a title, URL, optional icon, and click tracking. The page theme controls colors, fonts, button styles, and layout. When a visitor clicks any link on the page, it routes through the shortener for tracking before redirecting. The page owner can reorder links, add/remove links, and change the theme. Analytics show total page views plus per-link click counts and click-through rates.

### Key Classes to Add
```java
public class BioPage {
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private PageTheme theme;
    private List<BioLink> links; // ordered
    private long pageViews;
    
    public String render() { ... }
    public void reorderLinks(List<String> linkIds) { ... }
}

public class BioLink {
    private String id;
    private String title;
    private String url;
    private String icon; // optional
    private boolean active;
    private long clicks;
    
    public double getCTR(long pageViews) { return (double) clicks / pageViews; }
}

public class PageTheme {
    private String backgroundColor;
    private String buttonColor;
    private String textColor;
    private String fontFamily;
    private ButtonStyle buttonStyle; // ROUNDED, SQUARE, OUTLINE
}
```

---
*Copyright (c) 2026 Abhijay (abj). All rights reserved. Unauthorized copying, modification, or distribution prohibited.*
