package second;

import battlecode.common.*;

public class Carrier extends Robot {

    MapLocation myMine;
    boolean mining = true;

    IslandManager im;

    MicroCarrier microCarrier;

    static final int EXPLORE_LIMIT = 500;

    static final int DMG_THRESHOLD = 2;

    int enemyDetectedRound = -100;
    static final int CHICKEN_ROUNDS = 10;

    static final int MAX_RESOURCES = 39;

    static int dmg;

    static final int LAUNCHER_DMG = RobotType.LAUNCHER.damage;

    Carrier(RobotController rc) throws GameActionException {
        super(rc);
        myMine = hComm.targetOnSpawn;
        im = new IslandManager();
        microCarrier = new MicroCarrier();
    }

    void play() throws GameActionException {
        boolean didMicro = false;
        if (enemyDetected()){
            enemyDetectedRound = rc.getRoundNum();
            dmg = getDmg();
            if (dmg >= DMG_THRESHOLD && rc.isActionReady()){
                didMicro = microCarrier.doMicro();
                attack();
            }
        }

        updateMiningStatus();
        tryGettingAnchor();
        tryPuttingAnchor();
        tryMine();
        tryDeposit();
        if (!didMicro) moveToTarget();
        tryGettingAnchor();
        tryPuttingAnchor();
        tryMine();
        tryDeposit();
    }

    void updateMiningStatus(){
        if (mining && full()) mining = false;
        if (!mining && empty()) mining = true;
        if (myMine == null && explore.mapdata.carrierTarget != null) myMine = explore.mapdata.carrierTarget;
        if (myMine != null) rc.setIndicatorLine(rc.getLocation(), myMine, 0, 255, 0);
    }

    MapLocation getTarget() throws GameActionException {

        if (rc.getRoundNum() - enemyDetectedRound <= CHICKEN_ROUNDS) return hComm.myBaseLoc;
        //anchor stuff
        if (rc.getAnchor() != null) {
            MapLocation ans = getClosestIsland();
            if (ans == null) ans = im.getBestRandom(10);
            if (ans == null) ans = explore.getExploreTarget();
            return ans;
        }
        MapLocation anchorLoc = getAnchorLoc();
        if (anchorLoc != null) return anchorLoc;

        //non-anchor stuff
        MapLocation target = mining ? myMine : hComm.myBaseLoc;
        if (target == null)target = explore.getExploreTarget();
        if (target != null && rc.getLocation().distanceSquaredTo(target) <= 2){
            target = rc.getLocation();
        }
        return target;
    }

    void moveToTarget() throws GameActionException {
        MapLocation target = getTarget();
        pathfinder.moveTo(target);
    }

    void tryMine() throws GameActionException {
        if (!rc.isActionReady()) return;
        WellInfo[] nearbyWells = rc.senseNearbyWells(2);
        int x = GameConstants.WELL_STANDARD_RATE;
        int y = MAX_RESOURCES - getTotalAmount();
        if (y < x) x = y;
        if (x <= 0) return;
        for (WellInfo w : nearbyWells){
            if (rc.canCollectResource(w.getMapLocation(), x)){
                rc.collectResource(w.getMapLocation(), x);
                return;
            }
        }
    }

    void tryMoveAttack(){

    }

    void tryDeposit() throws GameActionException {
        if (!rc.isActionReady()) return;
        RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
        for (RobotInfo r : robots){
            if (r.getType() == RobotType.HEADQUARTERS){
                int a = rc.getResourceAmount(ResourceType.ADAMANTIUM);
                if (a > 0 && rc.canTransferResource(r.getLocation(), ResourceType.ADAMANTIUM, a)){
                    rc.transferResource(r.getLocation(), ResourceType.ADAMANTIUM, a);
                    return;
                }
                int m = rc.getResourceAmount(ResourceType.MANA);
                if (m > 0 && rc.canTransferResource(r.getLocation(), ResourceType.MANA, m)){
                    rc.transferResource(r.getLocation(), ResourceType.MANA, m);
                    return;
                }
                int e = rc.getResourceAmount(ResourceType.ELIXIR);
                if (e > 0 && rc.canTransferResource(r.getLocation(), ResourceType.ELIXIR, e)){
                    rc.transferResource(r.getLocation(), ResourceType.ELIXIR, e);
                    return;
                }
            }
        }
    }

    MapLocation getAnchorLoc() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
        for (RobotInfo r : robots){
            if (r.getType() == RobotType.HEADQUARTERS && r.getTotalAnchors() > 0) return r.getLocation();
        }
        return null;
    }

    void tryGettingAnchor() throws GameActionException {
        if (rc.getAnchor() != null) return;
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam());
        for (RobotInfo r : robots){
            if (r.getType() != RobotType.HEADQUARTERS) continue;
            if (r.getTotalAnchors() > 0){
                if (rc.canTakeAnchor(r.getLocation(), Anchor.STANDARD)){
                    rc.takeAnchor(r.getLocation(), Anchor.STANDARD);
                }
            }
        }
    }

    void tryPuttingAnchor() throws GameActionException {
        if (rc.getAnchor() == null) return;
        int id = rc.senseIsland(rc.getLocation());
        if (id < 0) return;
        if (rc.getTeam() == rc.senseTeamOccupyingIsland(id)) return;
        if (rc.canPlaceAnchor()) rc.placeAnchor();
    }

    int getTotalAmount(){
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR);
    }

    boolean full(){
        return getTotalAmount() >= MAX_RESOURCES;
    }

    boolean empty(){
        return getTotalAmount() == 0;
    }

    MapLocation getClosestIsland() throws GameActionException {
        int[] islands = rc.senseNearbyIslands();
        MapLocation ans = null;
        int d = rc.getType().visionRadiusSquared;
        MapLocation myLoc = rc.getLocation();
        for (int id : islands){
            Team t = rc.senseTeamOccupyingIsland(id);
            if (t == rc.getTeam()) continue;
            MapLocation[] iLocs = rc.senseNearbyIslandLocations(d, id);
            for (MapLocation m : iLocs){
                if (ans == null || m.distanceSquaredTo(myLoc) < d){
                    d = m.distanceSquaredTo(myLoc);
                    ans = m;
                }
            }
        }
        return ans;
    }

    boolean enemyDetected() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo r : enemies){
            if (r.getType() == RobotType.LAUNCHER) return true;
        }
        return false;
    }

    int getDmg(){
        int resourceSum = rc.getResourceAmount(ResourceType.ADAMANTIUM)
                + rc.getResourceAmount(ResourceType.MANA)
                + rc.getResourceAmount(ResourceType.ELIXIR);
        return (int) Math.floor(GameConstants.CARRIER_DAMAGE_FACTOR*(resourceSum));
    }

    boolean attack() throws GameActionException {
        if (!rc.isActionReady()) return false;
        AttackTarget bestAttacker = null;
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        for (RobotInfo r : enemies){
            if (r.getType() == RobotType.HEADQUARTERS || r.getType() == RobotType.CARRIER) continue;
            if (!rc.canAttack(r.getLocation())) continue;
            AttackTarget t = new AttackTarget(r);
            if(t.isBetterThan(bestAttacker)) bestAttacker = t;
        }
        if (bestAttacker != null){
            if (rc.canAttack(bestAttacker.mloc)) rc.attack(bestAttacker.mloc);
            return true;
        }
        return false;
    }

    class AttackTarget{
        RobotType type;
        int health;

        boolean effectiveDmg;
        boolean attacker = false;
        MapLocation mloc;

        boolean isBetterThan(AttackTarget t){
            if (t == null) return true;
            if (attacker & !t.attacker) return true;
            if (!attacker & t.attacker) return false;
            if (effectiveDmg && !t.effectiveDmg) return true;
            if (t.effectiveDmg && !effectiveDmg) return false;
            return health <= t.health;
        }

        AttackTarget(RobotInfo r){
            type = r.getType();
            health = r.getHealth();
            mloc = r.getLocation();
            switch(type){
                case LAUNCHER:
                case BOOSTER:
                case DESTABILIZER:
                    attacker = true;
                default:
                    break;
            }
            effectiveDmg = (dmg%LAUNCHER_DMG) >= (health%LAUNCHER_DMG);
        }
    }

    class IslandManager{

        int[][] exploredTimes;
        int index = 0;

        IslandManager(){
            exploredTimes = new int[rc.getMapWidth()][];
        }

        void fill(){
            while (index < exploredTimes.length){
                if (Clock.getBytecodesLeft() < EXPLORE_LIMIT) return;
                exploredTimes[index++] = new int[rc.getMapHeight()];
            }
        }

        void run() throws GameActionException {
            fill();
            if (isReady()){
                update();
            }
        }

        boolean isReady(){
            return index >= exploredTimes.length;
        }

        void update() throws GameActionException {
            if (!isReady()) return;
            if (rc.getAnchor() == null) return;
            int r = rc.getRoundNum();
            MapInfo[] tiles = rc.senseNearbyMapInfos();
            for (MapInfo m : tiles){
                if (Clock.getBytecodesLeft() < EXPLORE_LIMIT) return;
                exploredTimes[m.getMapLocation().x][m.getMapLocation().y] = r;
            }
        }

        MapLocation currentRandom;

        MapLocation getBestRandom(int tries){
            if (!isReady()) return null;
            int maxX = rc.getMapWidth();
            int maxY = rc.getMapHeight();
            while (tries-- > 0){
                MapLocation newLoc = new MapLocation((int)(Math.random()*maxX), (int)(Math.random()*maxY));
                if (currentRandom == null || exploredTimes[newLoc.x][newLoc.y] < exploredTimes[currentRandom.x][currentRandom.y]){
                    currentRandom = newLoc;
                }
            }
            return currentRandom;
        }

    }

}