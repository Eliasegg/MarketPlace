# 🛒 MarketPlace

MarketPlace is a feature-rich Minecraft plugin that creates a dynamic player-driven economy. It allows players to buy and sell items through an intuitive GUI interface, with support for both regular marketplace transactions and exciting Black Market deals.

## 🌟 Features

- Full NBT support!
- Inventory GUI interface for easy buying and selling w/ pagination support.
- Black Market with discounted items.
- Transaction history tracking.
- Integration with Vault for economy management.
- Discord webhook support for transaction notifications.
- Efficient caching system to reduce database queries.
- Asynchronous database operations for improved performance.
- Customizable messages and settings.

## 🛠️ Commands

- `/sell <price>` - List the item in your hand for sale in the marketplace.
- `/marketplace` - Open the main marketplace GUI to browse and buy items.
- `/blackmarket` - Access the Black Market for discounted items.
- `/transactions` - View your personal transaction history.

## 🔐 Permissions

- `marketplace.sell` - Allow use of /sell command.
- `marketplace.view` - Allow use of /marketplace command.
- `marketplace.blackmarket` - Allow use of /blackmarket command.
- `marketplace.history` - Allow use of /transactions command.

*Server operators have access to all commands by default.*

## 🧠 Technical Details / Features

### Caching System
MarketPlace implements an efficient caching system through the `ItemMarketplaceManager` class. This manager maintains an in-memory list of all current item listings, which is loaded when the plugin starts and updated as items are listed or sold. Significantly reduces database queries, especially when opening the marketplace or processing transactions.

### Black Market
Randomly selects a subset of items from the main marketplace and offers them at a 50% discount. However, the original seller still receives the full price.

### Transaction Handling
All transactions are processed asynchronously to ensure server performance isn't impacted. The plugin uses a cooldown system to prevent spam-clicking and potential exploits.

### Inventory Management
The plugin intelligently handles inventory management. If a player's inventory is full when purchasing an item, the item is automatically dropped naturally on the ground.

### Database Integration
MarketPlace uses MongoDB for persistent storage of player data, item listings, and transaction history. All database operations are performed asynchronously to maintain server performance.

### Discord Integration
The plugin supports sending notifications to a Discord channel via webhooks.

## 📦 Dependencies
- [Vault](https://www.spigotmc.org/resources/vault.34315/) for economy management
- An economy plugin for Vault to handle the economy. 
    - [BetterEconomy](https://www.spigotmc.org/resources/bettereconomy.96690/) 
    - [EssentialsX](https://essentialsx.net/downloads.html)

## 🛠️ Installation
- Place the MarketPlace.jar file in your server's `plugins` folder.
- Restart your server or load the plugin.
- Fill in the *mongodb* and *discord webhook* credentials in the `config.yml` file in the `plugins/MarketPlace` folder.
- Restart the server again to apply any configuration changes.

## 📝 Configuration

The `config.yml` file allows you to customize various aspects of the plugin, including:

- Database connection settings.
- Discord webhook URL.
- Purchase cooldown time.
- Custom messages for various actions.

## 📸 Screenshots

![image](https://i.imgur.com/rzR8PEy.png)

![image](https://i.imgur.com/Juwyj3t.png)

