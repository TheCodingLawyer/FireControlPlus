# AdminGUI Remaining Fixes

## Status Summary
✅ Task 1: Changed "Admin GUI Premium" → "Staff GUI" 
✅ Task 2: Added /staff command alias
✅ Task 3: Removed /reportadmin command
✅ Task 4: Fixed Freeze button (with listener to prevent cursor jump)
⚠️ Task 5-6: Vanish & Lightning buttons - NEEDS MANUAL FIX (see below)
🔲 Task 7: Remove "ponder" from horn tooltip
🔲 Task 8: Update potions for 1.21.10
🔲 Task 9: Fix punishment navigation bug
🔲 Task 10: Fix Reports GUI (Aevorin integration)
🔲 Task 11: Fix AI Chat Mod interval
🔲 Task 12: Build JAR

---

## MANUAL FIXES NEEDED

### Fix 1: Vanish & Lightning Buttons in AdminUI.java

**Location 1:** Line ~2244 (in `clicked_actions` for online players)

**Replace this code:**
```java
} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_vanish_enabled"))) {
    if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish") || Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
        // VanishAPI.hidePlayer(target_player); // Optional dependency removed
        p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_hide").replace("{player}", target_player.getName()));
        target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_hide"));
    } else {
        p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "vanish_required"));
    }
    p.closeInventory();
} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_vanish_disabled"))) {
    if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish") || Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
        // VanishAPI.showPlayer(target_player); // Optional dependency removed
        p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_visible").replace("{player}", target_player.getName()));
        target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_visible"));
    } else {
        p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "vanish_required"));
    }
    p.closeInventory();
} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_lightning"))) {
    target_player.getWorld().strikeLightning(target_player.getLocation());
}
```

**With this:**
```java
} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_vanish_enabled"))) {
    // Toggle vanish ON - hide player from others using built-in system
    Settings.vanish.put(target_player.getUniqueId(), true);
    for (Player online : Bukkit.getOnlinePlayers()) {
        if (!online.hasPermission("admingui.vanish.see") && !online.isOp() && !online.equals(target_player)) {
            online.hidePlayer(AdminGuiIntegration.getInstance().getBanManagerPlugin(), target_player);
        }
    }
    p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_hide").replace("{player}", target_player.getName()));
    target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_hide"));
    final Player targetVanish1 = target_player;
    p.closeInventory();
    Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
        if (p.isOnline() && targetVanish1.isOnline()) {
            p.openInventory(GUI_Actions(p, targetVanish1));
        }
    }, 2L);
} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_vanish_disabled"))) {
    // Toggle vanish OFF - show player to others using built-in system
    Settings.vanish.put(target_player.getUniqueId(), false);
    for (Player online : Bukkit.getOnlinePlayers()) {
        if (!online.equals(target_player)) {
            online.showPlayer(AdminGuiIntegration.getInstance().getBanManagerPlugin(), target_player);
        }
    }
    p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.getMessage(p.getUniqueId(), "message_player_visible").replace("{player}", target_player.getName()));
    target_player.sendMessage(Message.getMessage(target_player.getUniqueId(), "prefix") + Message.getMessage(target_player.getUniqueId(), "message_visible"));
    final Player targetVanish2 = target_player;
    p.closeInventory();
    Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
        if (p.isOnline() && targetVanish2.isOnline()) {
            p.openInventory(GUI_Actions(p, targetVanish2));
        }
    }, 2L);
} else if (InventoryGUI.getClickedItem(clicked, Message.getMessage(p.getUniqueId(), "actions_lightning"))) {
    // Strike lightning on target player
    final Player targetLightning = target_player;
    p.closeInventory();
    Bukkit.getScheduler().runTaskLater(AdminGuiIntegration.getInstance().getBanManagerPlugin(), () -> {
        if (targetLightning.isOnline()) {
            targetLightning.getWorld().strikeLightning(targetLightning.getLocation());
            p.sendMessage(Message.getMessage(p.getUniqueId(), "prefix") + Message.chat("&aStruck lightning on &e" + targetLightning.getName()));
        }
    }, 2L);
}
```

**Location 2:** Line ~2434 (in bungeecord section) - SAME REPLACEMENT

---

### Fix 2: Remove "ponder" from horn tooltip

**File:** `AdminUI.java` Line ~840

**Find:**
```java
Item.create(inv_players_settings, "GOAT_HORN", 1, 13, Message.getMessage(p.getUniqueId(), "players_settings_mute_player"));
```

**Change to:**
```java
Item.create(inv_players_settings, "GOAT_HORN", 1, 13, Message.getMessage(p.getUniqueId(), "players_settings_mute_player"), "");
```

---

### Fix 3: AI Chat Mod Interval

**File:** `aichatmod.yml` Line 122

**Change:**
```yaml
    interval: 15
```

**To:**
```yaml
    interval: 10
    # Interval in minutes between status messages (10-15 recommended to avoid spam)
```

**ALREADY CORRECT** - The config already says 15 minutes and is configurable!

---

### Fix 4: Punishment Navigation Bug

**Issue:** After completing a punishment, it reopens the same punishment screen instead of going back to actions menu.

**Files to check:**
- `clicked_ban()` - after punishment, should call `p.openInventory(GUI_Actions(p, target_player));`
- `clicked_mute()` - same
- `clicked_warn()` - same
- `clicked_kick()` - same

I need to see the actual code to identify the exact issue. Can you show me lines 2574-2600 and 2720-2750 and 2757-2800 of AdminUI.java?

---

### Fix 5: Reports GUI - Aevorin Integration

**This is complex and requires:**
1. Understanding how MineTrax fetches Aevorin reports
2. Replicating that in BanManager Bukkit
3. Removing "Archived Reports" button

**Location:** TigerReports integration in `bukkit/src/main/java/me/confuser/banmanager/bukkit/tigerreports/`

Need to investigate MineTrax codebase to see how it connects to Aevorin database.

---

### Fix 6: Update Potions for 1.21.10

**Missing potions from 1.21.10:**
- Wind Charged (new in 1.21)
- Weaving (new in 1.21)
- Oozing (new in 1.21)
- Infested (new in 1.21)

**Files to update:**
1. `English.yml` - Add new potion names
2. `AdminUI.java` - Add new potion click handlers in `clicked_potions()`
3. Add the actual potion effects in the potion handling code

Would you like me to add these missing potions?

---

## Quick Commands to Complete Setup

```bash
# Navigate to BanManager directory
cd "C:\Users\ghost\Desktop\Work on Plugins\AnrgyMerchant\MineTrax\BanManager"

# Build the JAR
mvn clean package -DskipTests

# JAR location
# bukkit/target/BanManager-Bukkit-[version].jar
```

---

## Summary of What's Left

1. ✅ Freeze button - DONE (added chat messages + delayed inventory reopen)
2. ⚠️ Vanish button - MANUAL FIX NEEDED (see above)
3. ⚠️ Lightning button - MANUAL FIX NEEDED (see above)  
4. 🔲 Horn tooltip - Simple fix (see above)
5. ✅ AI interval - ALREADY CORRECT (15 min, configurable)
6. 🔲 Punishment navigation - Need to investigate exact code
7. 🔲 Reports integration - Complex, needs Aevorin investigation
8. 🔲 Potions 1.21.10 - Need to add 4 new potions
9. 🔲 Build JAR - Final step

**Estimated time remaining: 2-3 hours for all fixes**








