package fortytwo;

import battlecode.common.*;

public class Carrier extends Robot {



    MicroCarrier microCarrier;

    static final int DMG_THRESHOLD = 20;

    static final int CHICKEN_ROUNDS = 4;

    static final int MAX_RESOURCES = 40;

    static final int MIN_WEIGHT_BASE = 10;

    static final int MIN_MANA = 5;

    static MapLocation adamantiumMine = null;
    static MapLocation manaMine = null;

    IslandManager im;

    static int dmg;

    MapLocation myBase = null;
    MapLocation suggestedMine = null;
    MapLocation myMine = null;

    MapLocation currentTarget = null;
    boolean didMicro = false;

    static final int MINING = 0;
    static final int TO_BASE = 1;
    static final int TO_ELIXIR = 2;
    int miningState = MINING;

    boolean elixirFinished = false;

    int baseIndex = -1;
    int mineIndex = -1;

    int lastTurnMining = -1000;

    static final int LAUNCHER_DMG = RobotType.LAUNCHER.damage;

    static final int TURNS_SAFE = 4;

    static final int MIN_FREE_SLOTS_SUGGESTION = 1;
    static final int MIN_FREE_SLOTS_DUMP_SUGGESTION = 2;
    static final int MIN_FREE_SLOTS_RANDOM_MINE = 2;

    Carrier(RobotController rc) throws GameActionException {
        super(rc);
        microCarrier = new MicroCarrier();
        im = new IslandManager();
        setSuggestedMine(hComm.targetOnSpawn);
    }

    void play() throws GameActionException {
        //requestsBFSs();
        didMicro = false;
        if (roundSeen == rc.getRoundNum()){
            if (enemyForcesToGoBase()) miningState = TO_BASE;
            if (shouldAttack()){
                didMicro = microCarrier.doMicro(false);
                attack();
            }
            if(microCarrier.flee()) didMicro = true;
        }

        checkElixir();
        checkMana();

        tryGettingAnchor();
        tryPuttingAnchor();
        while(tryMine()){
            lastTurnMining = rc.getRoundNum();
        }
        tryDeposit();
        tryCreatingElixir();

        updateMiningStatus();
        if (!didMicro) moveToTarget();

        Robot.bytecodeDebug += "AFT_MV = " + Clock.getBytecodeNum() + " ";

        tryGettingAnchor();
        tryPuttingAnchor();
        while(tryMine()){
            lastTurnMining = rc.getRoundNum();
        }
        tryDeposit();
        tryCreatingElixir();
    }

    void checkMana() throws GameActionException {
        if (HeadquarterComm.targetCode == 0 || rc.getRoundNum() > 40) return;
        if (suggestedMine == null && myMine == null){
            /*for (int i = 0; i < 4; ++i){
                int code = (HeadquarterComm.hqIndex + i)%4;
                if (code == 0) code = 4;
                MapLocation manaLoc = explore.mapdata.getManaMine(code);
                if (manaLoc == null) continue;
                if (rc.getLocation().distanceSquaredTo(manaLoc) < 150){
                    setSuggestedMine(manaLoc);
                    return;
                }
            }*/
            MapLocation manaLoc = explore.mapdata.getManaMine(HeadquarterComm.hqIndex);
            if (manaLoc == null) return;
            //if (rc.getLocation().distanceSquaredTo(manaLoc) < 150){
            setSuggestedMine(manaLoc);
            //}
        }
    }

    boolean enemyForcesToGoBase(){
        return rc.getWeight() >= MIN_WEIGHT_BASE;
    }

    boolean shouldAttack(){
        int dmg = getDmg();
        if (dmg >= 10 && rc.getHealth() < 2*RobotType.LAUNCHER.damage) return true;
        if (rc.getResourceAmount(ResourceType.MANA) > MIN_MANA) return false;
        if (dmg < DMG_THRESHOLD) return false;
        return true;
    }

    void checkElixir() throws GameActionException {
        if (elixirFinished){
            if (miningState == TO_ELIXIR) miningState = TO_BASE;
        }
        if (rc.readSharedArray(MapData.ELIXIR_TRIGGER_CHANNEL) == 2) elixirFinished = true;
        MapLocation target = explore.mapdata.getElixirTarget();
        if (target == null) return;
        if (rc.canSenseLocation(target)){
            WellInfo w = rc.senseWell(target);
            if (w != null && w.getResourceType() == ResourceType.ELIXIR) elixirFinished = true;
        }
        if (elixirFinished){
            if (miningState == TO_ELIXIR) miningState = TO_BASE;
        }
    }

    boolean tryCreatingElixir() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (elixirFinished) return false;
        if (miningState != TO_ELIXIR) return false;
        MapLocation target = explore.mapdata.getElixirTarget();
        if (target == null) return false;
        if (rc.canTransferResource(target, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM))){
            rc.transferResource(target, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM));
            //System.out.println("THROWING ELIXIR");
            return true;
        }
        return false;
    }

    void setSuggestedMine(MapLocation loc) throws GameActionException {
        if (loc == null) return;

        if (suggestedMine != null){
            System.out.println("This should not happen!!!");
            return;
        }

        suggestedMine = loc;

        if (mineIndex >= 0){
            bfsManager.reset(mineIndex, suggestedMine);
        }
        else {
            mineIndex = bfsManager.requestBFS(suggestedMine);
        }

        myBase = hComm.getClosestHQ(suggestedMine);
        if (baseIndex >= 0){
            bfsManager.reset(baseIndex, myBase);
        }
        else {
            baseIndex = bfsManager.requestBFS(myBase);
        }
    }

    void setMine(MapLocation loc) throws GameActionException {
        if (loc == null) return;

        if (myMine != null){
            System.out.println("This should not happen!!!");
            return;
        }

        myMine = loc;
        if (suggestedMine != null){
            if (myMine.distanceSquaredTo(suggestedMine) > 0){
                if (mineIndex >= 0){
                    bfsManager.reset(mineIndex, myMine);
                }
                else {
                    mineIndex = bfsManager.requestBFS(myMine);
                }
            }
        } else{
            if (mineIndex >= 0) bfsManager.reset(mineIndex, myMine);
            else mineIndex = bfsManager.requestBFS(myMine);
        }

        mineIndex = bfsManager.requestBFS(myMine);

        if (myBase != null){
            MapLocation newBase = hComm.getClosestHQ(myMine);
            if (newBase.distanceSquaredTo(myBase) > 0){
                myBase = newBase;
                if (baseIndex >= 0) bfsManager.reset(baseIndex, myBase);
                else baseIndex = bfsManager.requestBFS(myBase);
            }
            hComm.myBaseLoc = myBase;
            hComm.searchIndex();
        } else {
            myBase = hComm.getClosestHQ(myMine);
            baseIndex = bfsManager.requestBFS(myBase);
        }
        suggestedMine = null;
    }

    void updateMiningStatus() throws GameActionException{
        if (miningState == MINING && full()){
            if (shouldGoElixir()) miningState = TO_ELIXIR;
            else miningState = TO_BASE;
        }
        if (miningState != MINING && empty()) miningState = MINING;
        checkNearbyMines();
        explore.mapdata.reportMana();
        if (manaMine != null && explore.mapdata.getManaMine(HeadquarterComm.hqIndex) == null){
            miningState = TO_BASE;
        }
    }

    boolean shouldGoElixir() throws GameActionException {
        //if (elixirFinished) return false;
        int tElixir = rc.readSharedArray(MapData.ELIXIR_TRIGGER_CHANNEL);
        if (tElixir != 1) return false;
        if (elixirFinished){
            if (rc.canWriteSharedArray(MapData.ELIXIR_TRIGGER_CHANNEL, 2)) rc.writeSharedArray(MapData.ELIXIR_TRIGGER_CHANNEL, 2);
            return false;
        }

        if (rc.getResourceAmount(ResourceType.ADAMANTIUM) < 30) return false;

        MapLocation loc = explore.mapdata.getElixirTarget();
        if (loc == null) return false;

        if (rc.getLocation().distanceSquaredTo(loc) > 400) return false;
        //System.out.println("GOING TO ELIXIR");
        return true;
    }

    void eliminateSuggestion(){
        suggestedMine = null;
    }

    void checkNearbyMines() throws GameActionException {
        if (myMine != null) return;
        if (suggestedMine != null) {
            checkSuggestedMine();
        }

        WellInfo[] wells = rc.senseNearbyWells();
        for (WellInfo w : wells){
            if (myMine != null || Clock.getBytecodeNum() > 6000) return;
            if (adamantiumMine == null && w.getResourceType() == ResourceType.ADAMANTIUM) adamantiumMine = w.getMapLocation();
            if (manaMine == null && w.getResourceType() == ResourceType.MANA) manaMine = w.getMapLocation();
            if (hComm.targetCode == 0 && w.getResourceType() == ResourceType.MANA) continue;
            else if(hComm.targetCode == 1 && w.getResourceType() == ResourceType.ADAMANTIUM) continue;
            checkWell(w);
        }
    }

    void checkSuggestedMine() throws GameActionException {
        if (suggestedMine == null) return;
        if (rc.canSenseLocation(suggestedMine)){
            WellInfo wInfo = rc.senseWell(suggestedMine);
            if(wInfo == null || wInfo.getResourceType() == ResourceType.NO_RESOURCE){
                eliminateSuggestion();
                return;
            }
        }
        if (!rc.canSenseLocation(suggestedMine)) return;
        if (rc.getLocation().distanceSquaredTo(suggestedMine) > 5) return;
        int f = getFreeLocations(suggestedMine);
        if (f < MIN_FREE_SLOTS_SUGGESTION){
            eliminateSuggestion();
        } else{
            setMine(suggestedMine);
        }
    }

    void checkWell(WellInfo w) throws GameActionException {
        int f = getFreeLocations(w.getMapLocation());
        if (suggestedMine == null){
            if (f >= MIN_FREE_SLOTS_RANDOM_MINE){
                setMine(w.getMapLocation());
            }
            return;
        }
        else {
            if (f >= MIN_FREE_SLOTS_DUMP_SUGGESTION){
                setMine(w.getMapLocation());
            }
        }
    }

    int getFreeLocations(MapLocation loc) throws GameActionException {
        int ans = 0;
        for (Direction dir : directions){
            MapLocation newLoc = loc.add(dir);
            if (!rc.canSenseLocation(newLoc)) continue;
            if (!rc.senseMapInfo(newLoc).isPassable()) continue;
            RobotInfo r = rc.senseRobotAtLocation(newLoc);
            if (r != null){
                if (r.getType() == RobotType.HEADQUARTERS) continue;
                if (r.getType() == RobotType.CARRIER && r.getTeam() == rc.getTeam() && r.getID() != rc.getID()) continue;
            }
            ++ans;
        }
        return ans;
    }

    MapLocation getBase(){
        if (myBase != null) return myBase;
        return hComm.myBaseLoc;
    }

    MapLocation getTarget() throws GameActionException {
        //RUN
        //if (rc.getRoundNum() - enemyDetectedRound <= CHICKEN_ROUNDS) return getBase();

        //ANCHOR STUFF
        if (rc.getAnchor() != null) {
            im.run();
            MapLocation ans = im.bestIsland;
            if (ans == null) ans = vt.getBestRandom(10);
            if (ans == null) ans = explore.getExploreTarget();
            return ans;
        }

        MapLocation anchorLoc = getAnchorLoc();
        if (anchorLoc != null) return anchorLoc;

        //MINING OR BASE?
        MapLocation target;
        switch(miningState){
            case MINING: target = getMineTarget(); break;
            case TO_BASE: target = getBase(); break;
            default: target = explore.mapdata.getElixirTarget();
        }
        if (target != null) return target;

        //EXPLORE RANDOM
        target = explore.getExploreTarget();
        if (target != null) return target;

        //EXPLORE BFS
        if (baseIndex >= 0){
            target = bfsManager.bfsList[baseIndex].getFront();
            if (target != null) return target;
        }

        //RANDOM UNCHECKED
        target = explore.getExploreTarget2(10);
        return target;
    }

    MapLocation getMineTarget(){
        if (myMine != null) return myMine;
        return suggestedMine;
    }

    void moveToTarget() throws GameActionException {
        if (didMicro) return;
        //Robot.bytecodeDebug += " (" + Clock.getBytecodeNum() + " ";
        currentTarget = getTarget();

        int oldCooldown = rc.getMovementCooldownTurns();
        while (rc.isMovementReady()){
            actualMovement();
            if (roundSeen == rc.getRoundNum()){
                if (enemyForcesToGoBase()) miningState = TO_BASE;
                if(microCarrier.flee()) didMicro = true;
            }
            int newCooldown = rc.getMovementCooldownTurns();
            if (oldCooldown == newCooldown) break;
            oldCooldown = newCooldown;
        }
    }

    void actualMovement() throws GameActionException {

        MapLocation mine = getMineTarget();
        if (mine != null && currentTarget.distanceSquaredTo(mine) <= 2 && rc.getLocation().distanceSquaredTo(mine) <= 2){
            shuffleMine();
        }
        else if (currentTarget != null) {
            //rc.setIndicatorLine(rc.getLocation(), target, 150, 150, 150);
            if (myBase != null && currentTarget.distanceSquaredTo(myBase) == 0 && baseIndex >= 0)
                pathfinder.moveTo(baseIndex);
            else if (mine != null && currentTarget.distanceSquaredTo(mine) == 0 && mineIndex >= 0)
                pathfinder.moveTo(mineIndex);
            else pathfinder.moveTo(currentTarget);

        }


    }

    boolean tryMine() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (full()) return false;
        MapLocation mine = getMineTarget();
        if (mine != null && rc.canCollectResource(mine, 1)){
            rc.collectResource(mine, 1);
            return true;
        }
        MapLocation myLoc = rc.getLocation();
        MapLocation loc = myLoc;
        if (rc.canCollectResource(loc, 1)){
            rc.collectResource(loc, 1);
            return true;
        }
        loc = myLoc.add(Direction.EAST);
        if (rc.canCollectResource(loc,1)){
            rc.collectResource(loc, 1);
            return true;
        }
        loc = myLoc.add(Direction.NORTHEAST);
        if (rc.canCollectResource(loc,1)){
            rc.collectResource(loc, 1);
            return true;
        }
        loc = myLoc.add(Direction.NORTH);
        if (rc.canCollectResource(loc,1)){
            rc.collectResource(loc, 1);
            return true;
        }
        loc = myLoc.add(Direction.NORTHWEST);
        if (rc.canCollectResource(loc,1)){
            rc.collectResource(loc, 1);
            return true;
        }
        loc = myLoc.add(Direction.WEST);
        if (rc.canCollectResource(loc,1)){
            rc.collectResource(loc, 1);
            return true;
        }
        loc = myLoc.add(Direction.SOUTHWEST);
        if (rc.canCollectResource(loc,1)){
            rc.collectResource(loc, 1);
            return true;
        }loc = myLoc.add(Direction.SOUTH);
        if (rc.canCollectResource(loc,1)){
            rc.collectResource(loc, 1);
            return true;
        }loc = myLoc.add(Direction.SOUTHEAST);
        if (rc.canCollectResource(loc,1)) {
            rc.collectResource(loc, 1);
            return true;
        }
        return false;
    }

    boolean tryDeposit() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (lastTurnMining >= rc.getRoundNum() - 1 && rc.getWeight() < MAX_RESOURCES) return false;
        //if (miningState != TO_BASE) return false;

        RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
        for (RobotInfo r : robots){
            if (r.getType() == RobotType.HEADQUARTERS){
                int a = rc.getResourceAmount(ResourceType.ADAMANTIUM);
                if (a > 0 && rc.canTransferResource(r.getLocation(), ResourceType.ADAMANTIUM, a)){
                    rc.transferResource(r.getLocation(), ResourceType.ADAMANTIUM, a);
                    updateMiningStatus();
                    return true;
                }
                int m = rc.getResourceAmount(ResourceType.MANA);
                if (m > 0 && rc.canTransferResource(r.getLocation(), ResourceType.MANA, m)){
                    rc.transferResource(r.getLocation(), ResourceType.MANA, m);
                    updateMiningStatus();
                    return true;
                }
                int e = rc.getResourceAmount(ResourceType.ELIXIR);
                if (e > 0 && rc.canTransferResource(r.getLocation(), ResourceType.ELIXIR, e)){
                    rc.transferResource(r.getLocation(), ResourceType.ELIXIR, e);
                    updateMiningStatus();
                    return true;
                }
            }
        }
        return false;
    }

    MapLocation getAnchorLoc() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
        for (RobotInfo r : robots){
            if (r.getType() == RobotType.HEADQUARTERS && r.getTotalAnchors() > 0) return r.getLocation();
        }
        return null;
    }

    boolean tryGettingAnchor() throws GameActionException {
        if (rc.getAnchor() != null) return false;
        RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam());
        for (RobotInfo r : robots){
            if (r.getType() != RobotType.HEADQUARTERS) continue;
            if (r.getTotalAnchors() > 0){
                if (rc.canTakeAnchor(r.getLocation(), Anchor.STANDARD)){
                    rc.takeAnchor(r.getLocation(), Anchor.STANDARD);
                    //vt.reset();
                    return true;
                }
            }
        }
        return false;
    }

    boolean tryPuttingAnchor() throws GameActionException {
        if (rc.getAnchor() == null) return false;
        int id = rc.senseIsland(rc.getLocation());
        if (id < 0) return false;
        if (rc.getTeam() == rc.senseTeamOccupyingIsland(id)) return false;
        if (rc.canPlaceAnchor()){
            rc.placeAnchor();
            im.reset();
            return true;
        }
        return false;
    }

    int getTotalAmount(){
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR);
    }

    boolean full(){
        return rc.getWeight() >= MAX_RESOURCES;
    }

    boolean empty(){
        return rc.getWeight() == 0;
    }

    /*boolean enemyDetected() throws GameActionException {
        RobotInfo[] enemies = Robot.enemies;
        for (RobotInfo r : enemies){
            if (r.getType() == RobotType.LAUNCHER) return true;
        }
        return false;
    }*/

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

    MineLoc[] mineLocs;


    boolean shuffleMine() throws GameActionException {
        if (mineLocs == null){
            mineLocs = new MineLoc[9];
            for (Direction dir : directions) mineLocs[dir.ordinal()] = new MineLoc(dir);
        }


        MineLoc bestLoc = null;
        for (Direction dir : directions){
            MineLoc ml = mineLocs[dir.ordinal()];
            ml.update();
            if (ml.isBetterThan(bestLoc)) bestLoc = ml;
        }
        if (bestLoc != null && rc.canMove(bestLoc.dir)){
            Robot.move(bestLoc.dir);
            rc.setIndicatorDot(bestLoc.newLoc, 150, 150, 150);
            return true;
        }
        return false;
    }

    class MineLoc {
        Direction dir;
        MapLocation newLoc;
        int distToMine;
        int distToBase;
        boolean canMove;

        MineLoc(Direction dir){
            this.dir = dir;
        }

        void update(){
            newLoc = Robot.adjacentTiles.dirDanger[dir.ordinal()].endLoc;
            canMove = (dir == Direction.CENTER || rc.canMove(dir));
            if (AdjacentTiles.dirDanger[dir.ordinal()].closeToEnemyHQ) canMove = false;
            if (myBase != null) distToBase = newLoc.distanceSquaredTo(myBase);
            else distToBase = 100;
            distToMine = newLoc.distanceSquaredTo(getMineTarget());
        }

        boolean isBetterThan (MineLoc ml){
            if (ml == null) return true;

            if (!canMove) return false;
            if (!ml.canMove) return true;

            if (distToMine > 2) return false;
            if (ml.distToMine > 2) return true;

            if (rc.getWeight() < 10){
                if (distToBase > ml.distToBase) return true;
                if (distToBase < ml.distToBase) return false;
                if (ml.dir == Direction.CENTER) return true;
                return false;
            } else if(rc.getWeight() <= 30){
                if (ml.dir == Direction.CENTER) return true;
                return false;
            } else {
                if (distToBase < ml.distToBase) return true;
                if (distToBase > ml.distToBase) return false;
                if (ml.dir == Direction.CENTER) return true;
                return false;
            }
        }
    }
}