# LLD Learn - Quick Start Guide

## Starting the Development Server

```bash
# Navigate to website directory
cd website

# Start the server
node server.js
```

**Output:**
```
LLD site + run server: http://localhost:8080/
POST   /api/run          - compile + execute
POST   /api/save-edits   - save edits to disk
GET    /api/load-edits   - load edits from disk
DELETE /api/clear-edits  - clear all edits
Edits stored in: /path/to/website/.edits/edits.json
```

## Opening the Website

Open your browser to: **http://localhost:8080/**

## Features

### 📝 Edit Java Files
1. Click on any problem (e.g., "01-parking-lot")
2. Navigate to a Java file (e.g., "naive/Main.java")
3. Click **"Edit"** button
4. Make your changes in the Monaco editor
5. Click **"Save"** button (or press Ctrl/Cmd+S)

### 💾 Save Your Work
- **Save Button**: Click the green "Save" button
- **Keyboard**: Press **Ctrl+S** (Windows/Linux) or **Cmd+S** (Mac)
- **Storage**: Edits saved to `website/.edits/edits.json`
- **Persistence**: Survives browser restart and server restart

### ▶️ Run Java Code
1. Make sure you're viewing a Main.java file
2. Click **"Run"** button
3. Code is compiled and executed on the server
4. See output in the panel below

### 🧪 Run Tests (if available)
1. Click **"Tests"** button
2. Test cases are executed
3. See pass/fail results

### 📋 Copy Code
- Click **"Copy"** button to copy code to clipboard

## Multi-File Editing Workflow

```
1. Edit file A → Navigate away
2. Edit file B → Navigate away
3. Edit file C → Navigate away
4. Click "Save" → All 3 files saved to disk!
5. Close browser → Reopen later → All edits restored ✅
```

## Requirements

- **Node.js**: Version 18 or higher
- **Java JDK**: For running code (optional, for /api/run endpoint)

## File Structure

```
website/
  .edits/           # Your saved edits (git-ignored)
    edits.json      # All edits stored here
  server.js         # Backend server
  index.html        # Frontend
  app.js            # Frontend logic
  style.css         # Styling
  data.json         # Problem data
```

## Console Logging

Open browser DevTools (F12) to see:

```javascript
✅ Loaded edits from server: 5 files
✅ Saved 3 edits to server
⚠️ Server not available, falling back to localStorage
```

## Backing Up Your Edits

```bash
# Backup
cp website/.edits/edits.json ~/backups/my-edits-$(date +%Y%m%d).json

# Restore
cp ~/backups/my-edits-20260607.json website/.edits/edits.json
```

## Troubleshooting

### Port Already in Use
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill

# Or use a different port
PORT=3000 node server.js
```

### Edits Not Saving
1. Check server is running
2. Open browser console (F12)
3. Look for error messages
4. Verify `.edits/` directory exists

### Server Won't Start
```bash
# Check Node.js version
node --version  # Should be v18 or higher

# Check for syntax errors
node -c server.js
```

## Keyboard Shortcuts

- **Ctrl/Cmd+S**: Save all edits
- **Ctrl/Cmd+K**: Focus search
- **Escape**: Close file view / Close mobile menu

## Tips

1. **Edit multiple files** before saving - they're all kept in memory
2. **Save frequently** - Ctrl+S is quick
3. **Check console** - It shows what's happening
4. **Backup important work** - Copy `.edits/edits.json`
5. **Use Monaco features** - Autocomplete, syntax checking, etc.

## Next Steps

- Edit some problems!
- Try different solutions
- Run and test your code
- Build up your portfolio

---

**Happy coding!** 🚀

For issues or questions, check the console logs and the documentation in `.agents/artifacts/`.
