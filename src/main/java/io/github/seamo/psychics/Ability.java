package io.github.seamo.psychics;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

public class Ability extends JavaPlugin implements TabExecutor, Listener, CommandExecutor {

    public Map<String, String> playerTeams = new HashMap<>();
    private Map<String, Team> teams = new HashMap<>();
    private static Ability instance;
    private final Map<String, Boolean> resurrectedPlayers = new HashMap<>();
    private final Random random = new Random();

    @Override
    public void onEnable() {
        this.getCommand("psy").setExecutor(this);
        getCommand("psy").setTabCompleter(this);
        instance = this; // 플러그인 인스턴스를 저장
        Bukkit.getServer().getPluginManager().registerEvents(this, this); // 이벤트 리스너 등록
        Bukkit.getServer().getPluginManager().registerEvents(new OreDrop(),this);
    }

    public static Ability getInstance() {
        return instance;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        String action = args[0].toLowerCase();

        // 'remove' 처리
        if (action.equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                sender.sendMessage("플레이어 이름을 입력하세요.");
                return false;
            }

            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("플레이어를 찾을 수 없습니다.");
                return true;
            }

            return handleRemove(sender, player);
        }

        // 'attach' 처리
        if (args.length < 3) {
            return false;
        }

        String ability = args[1].toLowerCase();
        Player player = Bukkit.getPlayer(args[2]);

        if (player == null) {
            sender.sendMessage("플레이어를 찾을 수 없습니다.");
            return true;
        }

        if (action.equalsIgnoreCase("attach")) {
            return handleAttach(sender, ability, player);
        }

        return false;
    }

    private boolean handleAttach(CommandSender sender, String ability, Player player) {
        if (playerTeams.containsKey(player.getName())) {
            sender.sendMessage(player.getName() + " 이미 능력이 부여되어 있습니다.");
            return true;
        }

        playerTeams.put(player.getName(), ability);  // 능력 추가
        updatePlayerName(player, ability);

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        switch (ability) {
            case "paladin":
                givesword(player, "§d§l팔라딘의 검");
                player.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(0.5);
                break;
            case "resurrected":
                giveItem(player, Material.TOTEM_OF_UNDYING, Enchantment.BINDING_CURSE, 1, "§6§l소생자의 토템");
                break;
            case "guardian":
                Bukkit.dispatchCommand(console, "effect give " + player.getName() + " minecraft:resistance infinite 1 true");
                giveUnbreakableShield(player, "§1§l가디언의 방패");
                player.setHealthScale(40);
                break;
            case "weaponmaster":
                giveItem(player, Material.DIAMOND_SWORD, Enchantment.SHARPNESS, 6, "§2§l전설의 검");
                giveItem(player, Material.DIAMOND_AXE, Enchantment.SHARPNESS, 6, "§2§l전설의 도끼");
                giveItem(player, Material.BOW, Enchantment.POWER, 6, "§2§l전설의 활");
                giveItem(player, Material.ARROW, Enchantment.INFINITY, 1, "§2§l전설의 화살");
                break;
            case "assassin":
                Bukkit.dispatchCommand(console, "effect give " + player.getName() + " minecraft:speed infinite 1 true");
                int i;
                for (i = 0; i < 16; i++) {
                    giveItem(player, Material.ENDER_PEARL, Enchantment.BINDING_CURSE, 1, "§6§l어쎄신의 엔더진주");
                    giveItem(player, Material.WIND_CHARGE, Enchantment.BINDING_CURSE, 1, "§6§l어쎄신의 돌풍구");
                }
                player.getAttribute(Attribute.SCALE).setBaseValue(0.90);
                break;
            case "pyromancer":
                for (i = 0; i < 16; i++) {
                    giveItem(player, Material.FIRE_CHARGE, Enchantment.BINDING_CURSE, 1, "§4§l화염술사의 화염구");
                }

                Bukkit.dispatchCommand(console, "effect give " + player.getName() + " fire_resistance infinite 0 true");
                break;
            case "ninja":
                Bukkit.dispatchCommand(console, "effect give " + player.getName() + " minecraft:speed infinite 2 true");
                Bukkit.dispatchCommand(console, "effect give " + player.getName() + " minecraft:jump_boost infinite 2 true");
                break;
            case "ripper":
                giveItem(player, Material.CLOCK, Enchantment.BINDING_CURSE, 1, "§4§l사신의 시계");
                giveItem(player, Material.CLOCK, Enchantment.BINDING_CURSE, 1, "§4§l사신의 시계");
                giveItem(player, Material.CLOCK, Enchantment.BINDING_CURSE, 1, "§4§l사신의 시계");
                break;
            case "grap":
                player.setHealthScale(30);
                player.heal(30);
                giveItem(player, Material.BLAZE_ROD, Enchantment.BINDING_CURSE, 1, "§3§l블리츠 크랭크의 막대");
                giveItem(player, Material.BLAZE_ROD, Enchantment.BINDING_CURSE, 1, "§3§l블리츠 크랭크의 막대");
                giveItem(player, Material.BLAZE_ROD, Enchantment.BINDING_CURSE, 1, "§3§l블리츠 크랭크의 막대");
                break;
            case "gomu":
                player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).setBaseValue(8);
                player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).setBaseValue(6);
                break;
            default:
                sender.sendMessage("§c§l" + "알 수 없는 능력입니다.");
                return true;
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage("§2§l" + player.getName() + "에게 " + "§d§l" + ability + "§2§l" + " 능력이 부여되었습니다.");
        }

        return true;
    }

    private boolean handleRemove(CommandSender sender, Player player) {
        if (!playerTeams.containsKey(player.getName())) {
            sender.sendMessage(player.getName() + "에게 능력이 부여되지 않았습니다.");
            return true;
        }

        String oldAbility = playerTeams.get(player.getName());
        playerTeams.remove(player.getName());
        resetPlayerName(player);

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        switch (oldAbility) {
            case "paladin":
                Bukkit.dispatchCommand(console, "effect clear " + player.getName());
                player.getInventory().clear();
                player.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(0);
                break;
            case "resurrected":
                Bukkit.dispatchCommand(console, "effect clear " + player.getName());
                player.getInventory().clear();
                break;
            case "guardian":
                Bukkit.dispatchCommand(console, "effect clear " + player.getName());
                player.getInventory().clear();
                player.setHealthScale(20);
                break;
            case "weaponmaster":
                Bukkit.dispatchCommand(console, "effect clear " + player.getName());
                player.getInventory().clear();
                break;
            case "assassin":
                Bukkit.dispatchCommand(console, "effect clear " + player.getName());
                player.getInventory().clear();
                player.getAttribute(Attribute.SCALE).setBaseValue(1);
                break;
            case "pyromancer":
                Bukkit.dispatchCommand(console, "effect clear " + player.getName());
                player.getInventory().clear();
                break;
            case "ninja":
                Bukkit.dispatchCommand(console, "effect clear " + player.getName());
                player.getInventory().clear();
                Bukkit.dispatchCommand(console, "effect clear " + player.getName());
                break;
            case "ripper":
                Bukkit.dispatchCommand(console, "effect clear " + player.getName());
                player.getInventory().clear();
                break;
            case "grap":
                Bukkit.dispatchCommand(console, "effect clear " + player.getName());
                player.getInventory().clear();
                player.setHealthScale(20);
                break;
            case "gomu":
                Bukkit.dispatchCommand(console, "effect clear " + player.getName());
                player.getInventory().clear();
                player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).setBaseValue(4);
                player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).setBaseValue(3);
                break;
            default:
                sender.sendMessage("§c§l" + "알 수 없는 능력입니다.");
                return true;
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage("§2§l" + player.getName() + "에게 부여된 " + "§4§l" + oldAbility + "§2§l" + " 능력이 제거되었습니다.");
        }

        return true;
    }

    //이름 초기화 메소드
    private void resetPlayerName(Player player) {
        player.setPlayerListName(player.getName()); // Reset tab list name
        player.setDisplayName(player.getName());    // Reset chat name
    }

    //tab이나 이름 변경 메소드
    private void updatePlayerName(Player player, String ability) {
        String playerNameWithAbility = "§b§l" + player.getName() + "§2§l" + " (" + ability + ")";
        player.setPlayerListName(playerNameWithAbility); // Tab list name change
        player.setDisplayName(playerNameWithAbility);    // Chat name change
    }

    //아이템 지급 메소드
    private void giveItem(Player player, Material material, Enchantment enchantment, int level, String name) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (material == Material.BOW) {
            meta.addEnchant(Enchantment.INFINITY, 1, true);
        }
        if (enchantment != null) {
            meta.addEnchant(enchantment, level, true);
        }
        if (name != null) {
            meta.setItemName(name);
        }
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
    }

    //팔라딘 검 메소드
    private void givesword(Player player, String name) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.addEnchant(Enchantment.SHARPNESS, 7, true);
        meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
        meta.addEnchant(Enchantment.SWEEPING_EDGE, 3, true);
        meta.addEnchant(Enchantment.LOOTING, 3, true);
        meta.setItemName(name);
        sword.setItemMeta(meta);
        player.getInventory().addItem(sword);
    }

    //가디언 방패 메소드
    private void giveUnbreakableShield(Player player, String name) {
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemMeta meta = shield.getItemMeta();
        meta.setUnbreakable(true);
        meta.setItemName(name);
        shield.setItemMeta(meta);
        player.getInventory().addItem(shield);
    }

    //화염구 던지기 메소드
    @EventHandler
    public void onPlayerLeftClik(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (playerTeams.containsKey(player.getName()) && playerTeams.get(player.getName()).equals("pyromancer")) {
            if (!event.hasItem()) return;
            if (event.getItem().getType() != Material.FIRE_CHARGE) return;
            if (!event.getAction().name().contains("RIGHT_CLICK")) return;
            event.setCancelled(true);

            ItemStack item = event.getItem();

            if (item.getAmount() > 0) {
                Fireball fireball = player.launchProjectile(Fireball.class);
                Vector direction = player.getLocation().getDirection().multiply(2);
                fireball.setVelocity(direction);

                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;
        if (!Objects.equals(playerTeams.get(player.getName()), "grap")) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BLAZE_ROD) return;

        // 수정된 부분: 정확히 바라보는 플레이어만 감지
        Player target = getTargetPlayer(player, 25.0);
        if (target == null) return;

        // 🔹 신호기 소리 재생 (주변 플레이어도 들을 수 있도록)
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        player.sendMessage("§2§l끌어오는 대상: " + target.getName());
        target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);

        // 🔹 3초 동안 보라색 파티클 효과 적용
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 100) { // 3초 후 실행 (20틱 * 3초 = 60)
                    target.teleport(player.getLocation()); // 🔹 대상 순간이동
                    spawnFireworkEffect(target.getLocation()); // 🔹 폭죽 효과 발생
                    this.cancel();
                    return;
                }
                count++;
                target.spawnParticle(Particle.ELDER_GUARDIAN, target.getLocation(), 1, 0.5, 0.5, 0.5, 0.1);
            }
        }.runTaskTimer(Ability.getInstance(), 0L, 1L); // 1틱마다 반복 실행

        // 🔹 아이템 1개 감소
        item.setAmount(item.getAmount() - 1);
    }

    private Player getTargetPlayer(Player player, double range) {
        // 플레이어의 시선 방향 벡터
        Vector direction = player.getEyeLocation().getDirection();
        // 플레이어의 눈 위치
        Location startLocation = player.getEyeLocation();
        // 끝 위치를 시선 방향으로 range만큼 계산
        Location endLocation = startLocation.clone().add(direction.clone().multiply(range));

        for (Entity entity : player.getWorld().getEntities()) {
            // 자신을 제외하고 플레이어와의 거리 계산
            if (entity instanceof Player && entity != player) {
                Player targetPlayer = (Player) entity;

                // 플레이어와의 거리 계산
                double distance = startLocation.distance(targetPlayer.getLocation());

                if (distance <= range) {
                    // 플레이어가 시선 방향 상에 존재하는지 확인 (선과 플레이어의 위치를 비교)
                    if (isPlayerInLineOfSight(startLocation, endLocation, targetPlayer)) {
                        return targetPlayer;  // 시선 방향에 플레이어가 존재하면 해당 플레이어를 반환
                    }
                }
            }
        }
        return null;  // 범위 내에 플레이어가 없으면 null 반환
    }

    private boolean isPlayerInLineOfSight(Location startLocation, Location endLocation, Player targetPlayer) {
        // 플레이어의 위치와 시작점을 잇는 벡터
        Vector playerDirection = targetPlayer.getLocation().toVector().subtract(startLocation.toVector());

        // 시선 벡터와 플레이어 위치 벡터의 내적을 이용하여 선상에 있는지 확인
        double dotProduct = playerDirection.dot(endLocation.toVector().subtract(startLocation.toVector()));

        // 내적이 양수이면, 플레이어가 시선 범위 내에 존재한다고 판단
        return dotProduct > 0;
    }


    private void spawnFireworkEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        // 🔹 폭죽 터지는 소리 재생 (주변 모든 플레이어에게 들리게)
        world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.2f);

        // 🔹 폭죽 파티클 효과 (주변 플레이어도 볼 수 있도록)
        world.spawnParticle(Particle.FIREWORK, location, 30, 0.5, 0.5, 0.5, 0.1);
    }

    //부활자의 부활 메소드
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // "resurrected" 팀에 속한 플레이어만 해당
        if (playerTeams.containsKey(player.getName()) && playerTeams.get(player.getName()).equals("resurrected")) {
            boolean hasResurrectedBefore = resurrectedPlayers.getOrDefault(player.getName(), false);

            // 첫 부활은 100%, 이후에는 20% 확률로 부활
            if (!hasResurrectedBefore || random.nextDouble(100) < 50) { // 20% 확률로 부활
                event.setCancelled(true); // 플레이어의 사망 이벤트를 취소
                resurrectedPlayers.put(player.getName(), true); // 첫 부활 여부 저장

                player.sendMessage("§6§l부활했습니다!");
                player.setHealth(20.0); // 부활 후 체력 설정
                Totemevent(player); // 부활 시 불사의 토템 이벤트 발생
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1000, 0, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 500, 2, true, true));
            }
        }
    }

    private void Totemevent(Player player) {
        World world = player.getWorld();
        if (world == null) return;

        world.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 25) {
                    this.cancel(); // 반복 종료
                    return;
                }

                // 플레이어 위치에 불사의 토템 파티클 생성
                world.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 25);
                count++;
            }
        }.runTaskTimer(this, 0L, 1L); // 0틱 시작, 1틱 간격
    }

    @EventHandler
    public void onDamagePlayer(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!Objects.equals(playerTeams.get(player.getName()), "ninja")) return;
            if (random.nextDouble(100) < 10) {
                event.setCancelled(true);
                player.sendMessage("§2§l회피하였습니다!");
                avoid(player.getLocation());
            }
        }
    }

    private void avoid(Location loc) {
        World world = loc.getWorld();
        world.playSound(loc,  Sound.ITEM_ARMOR_EQUIP_CHAIN, 1f, 1f);
    }

    //리퍼의 시간 정지 메소드
    @EventHandler
    public void onPlayerLeftClick(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;

        Player player = event.getPlayer();
        if (!Objects.equals(playerTeams.get(player.getName()), "ripper")) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.CLOCK) return;

        // 4칸 이내의 가장 가까운 플레이어 찾기
        Player target = getNearestPlayer(player, 4.0); // 사거리 4칸
        if (target == null) return;

        // 효과 적용 (5초간 이동 불가 + 실명 + 점프 불가)
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, true, false)); // 5초 실명
        curseSound(target.getLocation());

        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 6, true, false));
        item.setAmount(item.getAmount() - 1);
    }

    private void curseSound(Location loc) {
        World world = loc.getWorld();
        world.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 1f);
    }

    private Player getNearestPlayer(Player player, double range) {
        Player nearest = null;
        double minDistance = range * range; // 거리 계산을 위해 제곱 값 사용

        for (Player p : player.getWorld().getPlayers()) {
            if (p == player) continue;
            if (!p.getLocation().getWorld().equals(player.getLocation().getWorld())) continue;

            double distance = p.getLocation().distanceSquared(player.getLocation());
            if (distance <= minDistance) {
                minDistance = distance;
                nearest = p;
            }
        }
        return nearest;
    }
    //tab자동완성
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("attach", "remove");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("attach")) {
                return Arrays.asList("paladin", "guardian", "weaponmaster", "assassin", "pyromancer", "ninja", "resurrected", "ripper", "grap", "gomu");

            }
            return null;
        }
        return null;
    }
}
