package twelve;

import battlecode.common.*;

public class Carrier extends Robot {



    MicroCarrier microCarrier;

    static final int EXPLORE_LIMIT = 500;

    static final int DMG_THRESHOLD = 2;

    int enemyDetectedRound = -100;
    static final int CHICKEN_ROUNDS = 4;

    static final int CHICKEN_ROUNDS_EXPLORER = 20;

    static final int MAX_RESOURCES = 40;

    IslandManager im;

    static int dmg;

    MapLocation myBase = null;
    MapLocation myMine = null;
    boolean mining = true;
    int baseIndex = -1;
    int mineIndex = -1;

    int lastTurnMining = -1000;

    int endExplore;

    static final int LAUNCHER_DMG = RobotType.LAUNCHER.damage;

    Carrier(RobotController rc) throws GameActionException {
        super(rc);
        myMine = hComm.targetOnSpawn;
        myBase = hComm.myBaseLoc;
        microCarrier = new MicroCarrier();
        endExplore = (rc.getRoundNum()*3)/2;
        im = new IslandManager();
    }

    void requestsBFSs(){
        if (baseIndex < 0 && myBase != null) baseIndex = bfsManager.requestBFS(myBase);
        if (mineIndex < 0 && myMine != null) mineIndex = bfsManager.requestBFS(myMine);
    }

    void play() throws GameActionException {
        requestsBFSs();

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
        if (myMine == null && explore.mapdata.carrierTarget != null && rc.getRoundNum() > endExplore) myMine = explore.mapdata.carrierTarget;
        if (myMine != null) rc.setIndicatorLine(rc.getLocation(), myMine, 0, 255, 0);
    }

    MapLocation getTarget() throws GameActionException {

        if (rc.getRoundNum() - enemyDetectedRound <= CHICKEN_ROUNDS) return hComm.myBaseLoc;
        //anchor stuff
        if (rc.getAnchor() != null) {
            im.run();
            MapLocation ans = im.bestIsland;
            if (ans == null) ans = vt.getBestRandom(10);
            if (ans == null) ans = explore.getExploreTarget();
            return ans;
        }
        MapLocation anchorLoc = getAnchorLoc();
        if (anchorLoc != null) return anchorLoc;

        //non-anchor stuff
        MapLocation target = mining ? myMine : hComm.myBaseLoc;
        if (target != null && rc.getLocation().distanceSquaredTo(target) <= 2){
            target = rc.getLocation();
        }
        if (target == null){
            /*if (baseIndex >= 0){
                target = bfsManager.bfsList[baseIndex].getFront();
                if (target != null) return target;
            }*/
            target = explore.getExploreTarget();
        }
        return target;
    }

    void moveToTarget() throws GameActionException {
        MapLocation target = getTarget();
        if (target != null) {
            if (myBase != null && target.distanceSquaredTo(myBase) == 0 && baseIndex >= 0) pathfinder.moveTo(baseIndex);
            else if (myMine != null && target.distanceSquaredTo(myMine) == 0 && mineIndex >= 0) pathfinder.moveTo(mineIndex);
            else pathfinder.moveTo(target);
        }
    }

    void tryMine() throws GameActionException {
        if (!rc.isActionReady()) return;
        int x = GameConstants.WELL_STANDARD_RATE;
        int y = MAX_RESOURCES - getTotalAmount();
        if (y < x) x = y;
        if (x <= 0) return;
        if (myMine != null && rc.canCollectResource(myMine, x)){
            rc.collectResource(myMine, x);
            return;
        }
        MapLocation myLoc = rc.getLocation();
        if (rc.canCollectResource(myLoc,x)){
            rc.collectResource(myLoc, x);
            lastTurnMining = rc.getRoundNum();
            return;
        }
        if (rc.canCollectResource(myLoc.add(Direction.EAST),x)){
            rc.collectResource(myLoc.add(Direction.EAST), x);
            lastTurnMining = rc.getRoundNum();
            return;
        }
        if (rc.canCollectResource(myLoc.add(Direction.NORTHEAST),x)){
            rc.collectResource(myLoc.add(Direction.NORTHEAST), x);
            lastTurnMining = rc.getRoundNum();
            return;
        }
        if (rc.canCollectResource(myLoc.add(Direction.NORTH),x)) {
            rc.collectResource(myLoc.add(Direction.NORTH), x);
            lastTurnMining = rc.getRoundNum();
            return;
        }
        if (rc.canCollectResource(myLoc.add(Direction.NORTHWEST),x)){
            rc.collectResource(myLoc.add(Direction.NORTHWEST), x);
            lastTurnMining = rc.getRoundNum();
            return;
        }
        if (rc.canCollectResource(myLoc.add(Direction.WEST),x)){
            rc.collectResource(myLoc.add(Direction.WEST), x);
            lastTurnMining = rc.getRoundNum();
            return;
        }
        if (rc.canCollectResource(myLoc.add(Direction.SOUTHWEST),x)){
            rc.collectResource(myLoc.add(Direction.SOUTHWEST), x);
            lastTurnMining = rc.getRoundNum();
            return;
        }
        if (rc.canCollectResource(myLoc.add(Direction.SOUTH),x)){
            rc.collectResource(myLoc.add(Direction.SOUTH), x);
            lastTurnMining = rc.getRoundNum();
            return;
        }
        if (rc.canCollectResource(myLoc.add(Direction.SOUTHEAST),x)){
            rc.collectResource(myLoc.add(Direction.SOUTHEAST), x);
            lastTurnMining = rc.getRoundNum();
            return;
        }
    }

    void tryDeposit() throws GameActionException {
        if (!rc.isActionReady()) return;
        if (lastTurnMining >= rc.getRoundNum() - 1 && getTotalAmount() < MAX_RESOURCES) return;

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
        RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
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
        if (rc.canPlaceAnchor()){
            rc.placeAnchor();
            im.reset();
        }
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

        int minDistToIsland = 0;
        MapLocation bestIsland = null;

        int islandID = -1;

        IslandManager(){
            reset();
        }

        void reset(){
            bestIsland = null;
            minDistToIsland = rc.getType().visionRadiusSquared;
        }

        void run() throws GameActionException {
            checkCurrentIsland();
            checkIslands();
        }

        void checkCurrentIsland() throws GameActionException {
            if (bestIsland == null) return;
            int d = rc.getLocation().distanceSquaredTo(bestIsland);
            if (d < minDistToIsland) minDistToIsland = d;
            if (rc.canSenseLocation(bestIsland)){
                Team t = rc.senseTeamOccupyingIsland(islandID);
                if (t == rc.getTeam()) reset();
            }
        }

        void checkIslands() throws GameActionException {
            int[] islands = rc.senseNearbyIslands();
            MapLocation myLoc = rc.getLocation();
            for (int id : islands){
                Team t = rc.senseTeamOccupyingIsland(id);
                if (t == rc.getTeam()) continue;
                MapLocation[] iLocs = rc.senseNearbyIslandLocations(minDistToIsland, id);
                for (MapLocation m : iLocs){
                    if (bestIsland == null || m.distanceSquaredTo(myLoc) < minDistToIsland){
                        minDistToIsland = m.distanceSquaredTo(myLoc);
                        bestIsland = m;
                        islandID = id;
                    }
                }
            }
        }
    }

}