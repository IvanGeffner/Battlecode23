package pathfindingtest;

import battlecode.common.*;

public class MapData {

    static RobotController rc;

    static int[] mapInfo;

    static int H;
    static int W;

    static int HW;

    static boolean hq;

    static int MIN_BYTECODE_REMAINING = 1000;

    final static int MAP_CHANNELS = 45;

    final static int WELL_CHANNEL = 45;
    static int CYCLE_LENGTH;

    boolean firstToWrite = false;

    int[] carriersSent;

    WellData[] adamantWells = new WellData[150];
    int adamantIndex = 0;
    int adamantIndexFree = 0;
    WellData[] manaWells = new WellData[150];
    int manaIndex = 0;
    int manaIndexFree;
    WellData[] elixirWells = new WellData[150];
    int elixirIndex = 0;
    int elixirIndexfree = 0;

    MapLocation carrierTarget = null;
    int carrierTargetCode = 0;

    boolean carrier;


    MapData(){
        rc = Robot.rc;
        hq = rc.getType() == RobotType.HEADQUARTERS;
        carrier = rc.getType() == RobotType.CARRIER;
        W = rc.getMapWidth();
        H = rc.getMapHeight();
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
            if (canWrite) rc.writeSharedArray(i, code);
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

            rc.writeSharedArray(i, code);
        }
    }

    void update(int index, int code){ //TODO
        switch(code){
            case 13:
                if (hq && elixirIndex < 150) {
                    Constants.indicatorString += "WELLE++";
                    int x = index / H, y = index % H;
                    elixirWells[elixirIndex++] = new WellData(new MapLocation(x, y), ResourceType.ELIXIR);
                }
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
            carriersSent[aux/4] += (1 << (4*(aux%4)));
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
            case 12: return Robot.hComm.isMyHQ(loc) ? -1 : 1;
            default: return -1;
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
                if (rc.canWriteSharedArray(WELL_CHANNEL, myCode)) rc.writeSharedArray(WELL_CHANNEL, myCode);
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

}
