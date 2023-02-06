package fortytwo;

import battlecode.common.*;

public class MapData {

    static RobotController rc;

    static int[] mapInfo;

    static int H;
    static int W;

    static int HW;

    static boolean hq;

    static int MIN_BYTECODE_REMAINING = 1000;

    final static int MAP_CHANNELS = 38;

    final static int ENEMY_MANA_CHANNELS = 39;

    final static int MANA_CHANNMELS = 43;

    final static int ELIXIR_CHANNEL = 48;
    final static int ELIXIR_TRIGGER_CHANNEL = 49;

    final static int WELL_CHANNEL = 47;
    static int CYCLE_LENGTH;

    boolean firstToWrite = false;

    int[] carriersSent;

    static WellData[] adamantWells = new WellData[150];
    static int adamantIndex = 0;
    static int adamantIndexFree = 0;
    static WellData[] manaWells = new WellData[150];
    static int manaIndex = 0;
    static int manaIndexFree;

    MapLocation carrierTarget = null;
    int carrierTargetCode = 0;

    boolean carrier;


    MapData(){
        rc = Robot.rc;
        hq = rc.getType() == RobotType.HEADQUARTERS;
        carrier = rc.getType() == RobotType.CARRIER;
        W = Robot.W;
        H = Robot.H;
        HW = H*W;
        mapInfo = new int[(HW + 3)/4];
        carriersSent = new int[mapInfo.length];
        CYCLE_LENGTH = (mapInfo.length + MAP_CHANNELS - 1) / MAP_CHANNELS;
        if (rc.getType() == RobotType.HEADQUARTERS){
            MIN_BYTECODE_REMAINING = 9000;
        }
    }

    void run() throws GameActionException {
        updateVision();
    }

    void updateVision() throws GameActionException {
        if (Clock.getBytecodesLeft() < MIN_BYTECODE_REMAINING) return;
        if ( hq && rc.getRoundNum() >= 10) return;
        //if (!isReady()) return;
        boolean checkSym = rc.getRoundNum() >= 8;
        MapInfo[] info = rc.senseNearbyMapInfos();
        int code;
        int aux;
        for (MapInfo mi : info){
            if (Clock.getBytecodesLeft() < MIN_BYTECODE_REMAINING) break;
            aux = mi.getMapLocation().x*H + mi.getMapLocation().y;
            if ((mapInfo[aux/4]&(0xF << (4*(aux%4)))) != 0) continue;
            //rc.setIndicatorDot(mi.getMapLocation(), 255, 0, 0);
            switch(mi.getCurrentDirection()){
                case EAST: code = 1; break;
                case NORTHEAST: code = 2; break;
                case NORTH: code = 3; break;
                case NORTHWEST: code = 4; break;
                case WEST: code = 5; break;
                case SOUTHWEST: code = 6; break;
                case SOUTH: code = 7; break;
                case SOUTHEAST: code = 8; break;
                default: code = 9; break;
            }
            if (!mi.isPassable()) code = 10;
            else if (mi.hasCloud()) code = 11;
            else if (code == 9) {
                RobotInfo r = rc.senseRobotAtLocation(mi.getMapLocation());
                if (r != null && r.getType() == RobotType.HEADQUARTERS){
                    code = 12;
                }
                WellInfo w = rc.senseWell(mi.getMapLocation());
                if (w != null) {
                    switch (w.getResourceType()) {
                        case ELIXIR:
                            code = 13;
                            carrierTarget = w.getMapLocation();
                            carrierTargetCode = 1;
                            break;
                        case MANA:
                            code = 14;
                            carrierTarget = w.getMapLocation();
                            carrierTargetCode = 2;
                            break;
                        case ADAMANTIUM:
                            code = 15;
                            carrierTarget = w.getMapLocation();
                            carrierTargetCode = 3;
                            break;
                        default:
                            break;
                    }
                }
            }
            if(checkSym) Robot.otherComm.checkSyms(mi.getMapLocation(), code);
            update(aux, code);
            mapInfo[aux/4] |= (code << 4*(aux%4));
        }
    }

    void reportMap() throws GameActionException{
        if (firstToWrite) writeVisionFirst();
        else writeVision();
    }

    void writeVision() throws GameActionException {
        boolean canWrite = rc.canWriteSharedArray(0,0);
        int r = rc.getRoundNum();
        int start = (r%CYCLE_LENGTH)*MAP_CHANNELS;
        int f = MAP_CHANNELS;
        if (mapInfo.length - start < f) f = mapInfo.length - start;

        int code, oldCode;

        for (int i = 0; i < f; ++i){
            if (Clock.getBytecodesLeft() < MIN_BYTECODE_REMAINING) break;
            code = rc.readSharedArray(i);
            oldCode = mapInfo[start + i];

            code |= oldCode;
            if (code != oldCode) updateBulk(start + i, code, oldCode);

            mapInfo[start+i] = code;
            if (canWrite) Robot.write(i, code);
        }
    }

    void writeVisionFirst() throws GameActionException {
        int r = rc.getRoundNum();
        int start = (r%CYCLE_LENGTH)*MAP_CHANNELS;
        int prevStart = ((r-1)%CYCLE_LENGTH)*MAP_CHANNELS;
        int code, oldCode;

        for (int i = 0; i < MAP_CHANNELS; ++i){

            code = rc.readSharedArray(i);

            if (prevStart+i < mapInfo.length){
                oldCode = mapInfo[prevStart+i];

                code |= oldCode;
                if (code != oldCode) updateBulk(prevStart+i, code, oldCode);

                mapInfo[prevStart + i] = code;
            }
            if (start + i < mapInfo.length) code = mapInfo[start+i];
            else code = 0;

            Robot.write(i, code);
        }
    }

    void update(int index, int code){ //TODO
        switch(code){
            case 13:
                /*if (hq && elixirIndex < 150) {
                    Constants.indicatorString += "WELLE++";
                    int x = index / H, y = index % H;
                    elixirWells[elixirIndex++] = new WellData(new MapLocation(x, y), ResourceType.ELIXIR);
                }*/
                break;
            case 14:
                if (hq && manaIndex < 150) {
                    Constants.indicatorString += "WELLM++";
                    int x = index / H;
                    int y = index % H;
                    manaWells[manaIndex++] = new WellData(new MapLocation(x, y), ResourceType.MANA);
                }
                break;
            case 15:
                if (hq && adamantIndex < 150) {
                    Constants.indicatorString += "WELLA++";
                    int x = index / H;
                    int y = index % H;
                    adamantWells[adamantIndex++] = new WellData(new MapLocation(x, y), ResourceType.ADAMANTIUM);
                }
                break;
            default: break;
        }
    }

    void updateBulk(int index, int code, int oldCode){
        if ((code & 0xF000) != (oldCode & 0xF000)){
            update(4*index+3, (code >>> 12) & 0xF);
        }
        if ((code & 0x0F00) != (oldCode & 0x0F00)){
            update(4*index+2, (code >>> 8) & 0xF);
        }
        if ((code & 0x00F0) != (oldCode & 0x00F0)){
            update(4*index+1, (code >>> 4) & 0xF);
        }
        if ((code & 0x000F) != (oldCode & 0x000F)){
            update(4*index, code & 0xF);
        }
    }

    int getCode(MapLocation loc){
        int aux = loc.x*H + loc.y;
        return ((mapInfo[aux/4] >>> (4*(aux%4)))&0xF);
    }

    boolean explored(MapLocation loc){
        if (loc == null) return true;
        return getCode(loc) != 0;
    }

    WellData getBestAdamantium(int tries) throws GameActionException {
        WellData bestWell = null;
        int bestDist = 0;
        int maxTries = adamantIndex;
        if (maxTries > adamantIndexFree + tries) maxTries = adamantIndexFree + tries;
        for (int i = adamantIndexFree; i < maxTries; ++i){
            WellData ans = adamantWells[i];
            if (!ans.isFull()){
                if (bestWell == null || rc.getLocation().distanceSquaredTo(ans.loc) < bestDist){
                    bestDist = rc.getLocation().distanceSquaredTo(ans.loc);
                    bestWell = ans;
                }
            } else{
                if (bestWell == null) ++adamantIndexFree;
            }
        }
        return bestWell;
    }

    WellData getBestMana(int tries) throws GameActionException {
        WellData bestWell = null;
        int bestDist = 0;
        int maxTries = manaIndex;
        if (maxTries > manaIndexFree + tries) maxTries = manaIndexFree + tries;
        for (int i = manaIndexFree; i < maxTries; ++i){
            WellData ans = manaWells[i];
            if (!ans.isFull()){
                if (bestWell == null || rc.getLocation().distanceSquaredTo(ans.loc) < bestDist){
                    bestDist = rc.getLocation().distanceSquaredTo(ans.loc);
                    bestWell = ans;
                }
            } else{
                if (bestWell == null) ++manaIndexFree;
            }
        }
        return bestWell;
    }

    WellData getBestManaUnfiltered(int tries){
        WellData bestWell = null;
        int bestDist = 0;
        for (int i = 0; i < tries && i < manaIndex; ++i){
            WellData ans = manaWells[i];
            if (bestWell == null || rc.getLocation().distanceSquaredTo(ans.loc) < bestDist){
                bestDist = rc.getLocation().distanceSquaredTo(ans.loc);
                bestWell = ans;
            }
        }
        return bestWell;
    }

    void updateEnemyMana() throws GameActionException {
        WellData bestWell = getBestManaUnfiltered(10);
        if (bestWell == null) return;
        rc.writeSharedArray(ENEMY_MANA_CHANNELS + HeadquarterComm.hqIndex - 1, 1 + ((bestWell.loc.x << 6) | (bestWell.loc.y)));
    }

    MapLocation getBestEnemyWell() throws GameActionException {
        if (HeadquarterComm.myBaseLoc == null) {
            int code = rc.readSharedArray(ENEMY_MANA_CHANNELS + HeadquarterComm.hqIndex - 1);
            if (code-- == 0) return null;
            return Robot.otherComm.getSymmetric(new MapLocation((code >>> 6) & 0x3F, code & 0x3F));
        }

        MapLocation bestLoc = null;
        for (int i = 0; i < 4; ++i){
            if (Robot.otherComm.sym == null) return null;
            int code = rc.readSharedArray(ENEMY_MANA_CHANNELS + i);
            if (code-- == 0) continue;
            MapLocation loc = Robot.otherComm.getSymmetric(new MapLocation((code >>> 6) & 0x3F, code & 0x3F));
            if (bestLoc == null || HeadquarterComm.myBaseLoc.distanceSquaredTo(loc) < HeadquarterComm.myBaseLoc.distanceSquaredTo(bestLoc)){
                bestLoc = loc;
            }
        }
        return bestLoc;
    }

    class WellData{
        MapLocation loc;
        ResourceType type;
        Integer minDistance = null;

        WellData(MapLocation loc, ResourceType type){
            this.loc = loc;
            this.type = type;
        }

        int getCarriersSent(){
            int aux = loc.x*H + loc.y;
            return (carriersSent[aux/4] >>> (4*(aux%4)))&0xF;
        }

        void addCarrier(){
            int aux = loc.x*H + loc.y;
            carriersSent[aux/4] += (1 << (4*(aux%4)));
        }

        int getDistance() throws GameActionException {
            if (minDistance != null) return minDistance;
            if (rc.getRoundNum() <= 5) return 0;
            MapLocation hqLoc = Robot.hComm.getClosestHQ(loc);
            minDistance = Math.max(Math.abs(hqLoc.x - loc.x), Math.abs(hqLoc.y - loc.y));
            return minDistance;
        }

        boolean isFull() throws GameActionException {
            return getCarriersSent() >= 9 + (getDistance() + 2)/8;
        }

    }

    void checkCarriers() throws GameActionException {
        for (int i = 1; i <= 4; ++i){
            int code2 = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * i + 1);
            if (code2 == 0) return;
            int code = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2 * i);
            if (code == 0xFFFF) continue;
            int x = (code >>> 10) & 0x3F;
            int y = (code >>> 4) & 0x3F;
            int aux = x*H + y;
            int a = rc.getRoundNum() <= 3 ? 2 : 1; //TODO: check this shit :D
            carriersSent[aux/4] += (a << (4*(aux%4)));
        }
    }

    void addWells(){
        WellInfo[] wells = rc.senseNearbyWells();
        for (WellInfo w : wells){
            int aux = w.getMapLocation().x*H + w.getMapLocation().y;
            if ((mapInfo[aux/4]&(0xF << (4*(aux%4)))) != 0) continue;
            int code = 15;
            switch (w.getResourceType()) {
                case ELIXIR:
                    code = 13;
                    carrierTarget = w.getMapLocation();
                    carrierTargetCode = 1;
                    break;
                case MANA:
                    code = 14;
                    carrierTarget = w.getMapLocation();
                    carrierTargetCode = 2;
                    break;
                case ADAMANTIUM:
                    code = 15;
                    carrierTarget = w.getMapLocation();
                    carrierTargetCode = 3;
                    break;
                default:
                    break;
            }
            update(aux, code);
            mapInfo[aux/4] |= (code << 4*(aux%4));
        }
    }

    int isEnemyHQ(MapLocation loc) throws GameActionException {
        int aux = loc.x*H + loc.y;
        switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
            case 0: return 0;
            case 12: return Robot.hComm.isMyHQ(loc) ? 2 : 1;
            default: return 2;
        }
    }

    int prevWell = 0;
    boolean shouldReport = true;

    void updateWell() throws GameActionException {
        if (!shouldReport) return;
        if (rc.getRoundNum() <= 1) return;
        int newWell = rc.readSharedArray(WELL_CHANNEL);
        if (carrierTarget != null){
            int myCode = (carrierTargetCode << 12) | (carrierTarget.x << 6) | carrierTarget.y;
            if (newWell == myCode) shouldReport = false;
            if (shouldReport && newWell == prevWell){
                if (rc.canWriteSharedArray(WELL_CHANNEL, myCode)) Robot.write(WELL_CHANNEL, myCode);
            }
        }
        prevWell = newWell;
    }

    void checkWellArray() throws GameActionException {
        int newWell = rc.readSharedArray(WELL_CHANNEL);
        if (newWell == 0){
            Constants.indicatorString += "NO NEW WELL";
            return;
        }
        int x = (newWell >>> 6)&0x3F;
        int y = (newWell)&0x3F;
        rc.setIndicatorDot(new MapLocation(x,y), 0, 255, 0);
        int code;
        switch((newWell >>> 12)&0xF){
            case 1: code = 13;break;
            case 2: code = 14;break;
            default: code = 15;break;
        }
        Constants.indicatorString += "CODE "+ code;
        int aux = x*H + y;
        int oldCode = (mapInfo[aux/4] >>> (4*(aux%4)))&0xF;
        if (oldCode == 0){
            update(aux, code);
            mapInfo[aux/4] |= (code << 4*(aux%4));
        }
    }

    void checkManaWells() throws GameActionException {
        for (int i = 1; i <= 4; ++i){
            MapLocation loc = getManaMine(i);
            if (loc == null) continue;
            int aux = loc.x*H + loc.y;
            int oldCode = (mapInfo[aux/4] >>> (4*(aux%4)))&0xF;
            if (oldCode == 0){
                update(aux, 14);
                mapInfo[aux/4] |= (14 << 4*(aux%4));
            }
        }
    }


    void computeElixir() throws GameActionException {
        if (rc.getRoundNum() < 100) return;
        int code = rc.readSharedArray(ELIXIR_CHANNEL);
        if (code != 0){
            //System.out.println("ALREADY ELIXIR");
            return;
        }


        WellData bestWell = null;
        int bestDist = 0;
        int maxTries = 5;
        for (int i = 0; i < maxTries; ++i){
            WellData ans = manaWells[i];
            if (ans == null) break;
            for (int b = 1; b <= 4; ++b){
                code = rc.readSharedArray(GameConstants.SHARED_ARRAY_LENGTH - 2*b + 1);
                if (code == 0) continue;
                int x2 = (code >>> 10) & 0x3F;
                int y2 = (code >>> 4) & 0x3F;
                MapLocation hLoc = new MapLocation(x2, y2);
                if (bestWell == null || hLoc.distanceSquaredTo(ans.loc) < bestDist){
                    bestWell = ans;
                    bestDist = hLoc.distanceSquaredTo(ans.loc);
                }
            }
        }
        if (bestWell != null){
            rc.writeSharedArray(ELIXIR_CHANNEL, 1 + ((bestWell.loc.x << 6) | bestWell.loc.y));
            //System.out.println("Computed Elixir! At " + bestWell.loc.toString());
        }
    }

    MapLocation getElixirTarget() throws GameActionException{
        int code = rc.readSharedArray(ELIXIR_CHANNEL);
        if (code == 0) return null;
        code--;
        return new MapLocation((code >>> 6)&0x3F, (code&0x3F));
    }

    void reportMana() throws GameActionException {
        if (Carrier.manaMine == null) return;
        int channel = MANA_CHANNMELS + HeadquarterComm.hqIndex - 1;
        int code = (Carrier.manaMine.x << 6) | Carrier.manaMine.y;
        if (rc.canWriteSharedArray(channel, code+1)) rc.writeSharedArray(channel, code+1);
    }

    MapLocation getManaMine(int hqI) throws GameActionException {
        int code = rc.readSharedArray(MANA_CHANNMELS + hqI - 1);
        if (code-- == 0) return null;
        MapLocation ans = new MapLocation((code >> 6) & 0x3F, code&0x3F);
        rc.setIndicatorDot(ans, 0, 0, 0);
        return ans;
    }


}
