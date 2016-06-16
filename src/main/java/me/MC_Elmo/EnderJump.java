package me.MC_Elmo;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import static org.bukkit.ChatColor.GREEN;

/**
 * Created by Elom on 5/31/16.
 */
public class EnderJump extends JavaPlugin implements Listener
{

    private final String prefix  = "§8[§4Ender§3Jump§8]";
    private String title = ChatColor.STRIKETHROUGH + "-----" + ChatColor.RESET + prefix + ChatColor.RESET + ChatColor.STRIKETHROUGH + "-----";
    private FileConfiguration config = getConfig();
    private Logger logger = getLogger();
    private PluginDescriptionFile pdfFile = getDescription();
    private PluginManager pm = Bukkit.getServer().getPluginManager();
    private HashMap<UUID, Long> cooldowns = new HashMap<UUID, Long>();
    private HashMap<UUID, Boolean> justjumped = new HashMap<UUID, Boolean>();

    public void loadConfiguration()
    {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    public void onEnable()
    {
            loadConfiguration();
            pm.registerEvents(this,this);
            logger.info(prefix + " Plugin Version : " + pdfFile.getVersion());
            logger.info(prefix + " Plugin Created By MC_Elmo ");
    }
    public void onDisable()
    {
        cooldowns.clear();
        justjumped.clear();
    }
        @EventHandler
        public void onDamage(EntityDamageEvent event)
        {
            if(!(event.getEntity() instanceof Player))
            {
                return;
            }
            Player player = (Player) event.getEntity();
            if(event.getCause().equals(EntityDamageEvent.DamageCause.FALL))
            {
                if(justjumped.containsKey(player.getUniqueId()))
                {
                    justjumped.remove(player.getUniqueId());
                    event.setCancelled(true);
                    return;
                } else
                {
                    return;
                }
            }else
            {
                return;
            }
        }
        @EventHandler
        public void onClick(PlayerInteractEvent event)
        {

            if(!(config.getBoolean("EnderJump.enabled")))
            {
                return;
            }
            Player player = event.getPlayer();

                if(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
                {
                    return;
                }
            Location loc = player.getLocation();
            Action a = event.getAction();
            Material material = event.getMaterial();
            if(config.getList("EnderJump.jumpItems").contains(material.getId())&& (a == Action.LEFT_CLICK_AIR || a == Action.RIGHT_CLICK_AIR))
            {
                if(!(player.hasPermission("EnderJump.use")))
                {
                    return;
                }
                double Height = config.getDouble("EnderJump.Distance.JumpHeight");
                double Width = config.getDouble("EnderJump.Distance.JumpLength");
                double jumpHeight = Height / 9.75;
                double jumpWidth = Width / 2.5;
                int cooldownTime = config.getInt("EnderJump.Cooldown.time");
                boolean cooldown = config.getBoolean("EnderJump.Cooldown.enabled");
                UUID playerID = player.getUniqueId();
                    if(cooldown && cooldowns.containsKey(playerID))
                    {
                        long secondsLeft = ((cooldowns.get(playerID)/1000)+cooldownTime) - (System.currentTimeMillis()/1000);
                        if(secondsLeft > 0)
                        {
                            String cooldownMessage = ChatColor.translateAlternateColorCodes('&',config.getString("EnderJump.Cooldown.cooldownMessage"));
                            cooldownMessage = cooldownMessage.replace("{seconds}",String.valueOf(secondsLeft));
                            player.sendMessage(cooldownMessage);
                            return;
                        }
                    }

                if(cooldown && !(player.hasPermission("enderjump.bypassCooldown")))
                {
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                }
                player.setVelocity(player.getLocation().getDirection().setY(jumpHeight).multiply(jumpWidth));
                if(config.getBoolean("EnderJump.Sound & Effects.Sounds.Enable Sounds"))
                {
                    String soundString = config.getString("EnderJump.Sound & Effects.Sounds.Sound").toUpperCase();
                    Sound sound = Sound.valueOf(soundString);
                    Location playerLoc = player.getLocation();
                    player.playSound(playerLoc, sound,100.0F,100.0F);
                }
                if(config.getBoolean("EnderJump.Food & Damage.Disable Fall Damage"))
                {
                    justjumped.put(playerID, true);
                }
                if(config.getBoolean("EnderJump.Food & Damage.Hunger.Costs Hunger") && !(player.hasPermission("enderjump.bypassHunger")))
                {
                    int cost = config.getInt("EnderJump.Food & Damage.Hunger.Hunger Cost");
                    player.setFoodLevel(player.getFoodLevel() - (cost * 2));
                }
                if(config.getBoolean("EnderJump.Sound & Effects.Effects.Enable Effects"))
                {
                    String effectString = config.getString("EnderJump.Sound & Effects.Effects.Effect").toUpperCase();
                    Effect effect = Effect.valueOf(effectString);
                    Location playerLoc = player.getLocation();


                        for(Player all : Bukkit.getOnlinePlayers())
                        {
                            all.playEffect(playerLoc, effect, 4);
                        }
                }
                if(config.getBoolean("EnderJump.Messages.Enable Message"))
                {
                    String message = ChatColor.translateAlternateColorCodes('&',config.getString("EnderJump.Messages.Message"));
                    player.sendMessage(message);
                }
            }
        }








    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("enderjump") || cmd.getName().equalsIgnoreCase("ej"))
        {
            if ((args.length == 0))
            {
                sender.sendMessage(title);
                sender.sendMessage(GREEN + "/enderjump help : " + ChatColor.DARK_GREEN + "Display Plugin Help.");
                if (sender.hasPermission("enderjump.reload"))
                {
                    sender.sendMessage(GREEN + "/enderjump reload : " + ChatColor.DARK_GREEN + "Reload the Config.");
                }
                return true;
            } else if (args.length == 1)
            {
                if (args[0].equalsIgnoreCase("reload"))
                {
                    if (sender.hasPermission("enderjump.reload") || sender.isOp())
                    {
                        reloadConfig();
                        saveConfig();
                        config = getConfig();
                        sender.sendMessage(prefix + "§aReloaded Config Successfully!");
                        logger.info(sender.getName() + " reloaded " + (pdfFile.getFullName()));
                        return true;
                    } else
                    {
                        sender.sendMessage(prefix + "§4You do not have permission to use this command!");
                        return false;
                    }
                } else if (args[0].equalsIgnoreCase("help"))
                {
                    sender.sendMessage(title);
                    sender.sendMessage(GREEN + "/enderjump help : " + ChatColor.DARK_GREEN + "Display Plugin Help.");
                    if (sender.hasPermission("enderjump.reload"))
                    {
                        sender.sendMessage(GREEN + "/enderjump reload : " + ChatColor.DARK_GREEN + "Reload the Config.");
                    }
                } else
                {
                    sender.sendMessage(prefix + "§4Invalid Argument! ");
                    sender.sendMessage(prefix + "§4Try /enderjump help");
                    return false;
                }
            } else
            {
                sender.sendMessage(prefix + " §4Too many arguments!");
                sender.sendMessage(prefix + "§4Try /enderjump help");
                return false;
            }

        }
        return true;
    }
}
