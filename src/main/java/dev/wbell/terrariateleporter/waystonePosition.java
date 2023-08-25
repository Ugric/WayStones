package dev.wbell.terrariateleporter;

import com.google.gson.Gson;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class waystonePosition {

    private static final Gson gson = new Gson();
    private static File dataFile = new File("");
    public static final List<PositionData> positions = new ArrayList<PositionData>();
    public static PositionData waystoneNear(PositionData pos) {
        for (int height = -1; height <= 2; height++) {
            for (int width = -1; width <= 1; width++) {
                for (int length = -1; length <= 1; length++) {
                    if (waystoneExists(new PositionData(pos.getX() + width, pos.getY() + height, pos.getZ() + length))) {
                        return new PositionData(pos.getX() + width, pos.getY() + height, pos.getZ() + length);
                    }
                }
            }
        }
        for (PositionData position : positions) {
            if (position.getX() == pos.getX() && position.getY() == pos.getY() && position.getZ() == pos.getZ()) {
                return position;
            }
        }
        return null;
    }
    public static boolean waystoneExists(PositionData pos) {
        for (PositionData position : positions) {
            if (position.getX() == pos.getX() && position.getY() == pos.getY() && position.getZ() == pos.getZ()) {
                return true;
            }
        }
        return false;
    }

    public static void addWaystone(PositionData position) {
        positions.add(position);
        savePositions();
    }

    public static void removeWaystone(PositionData position) {
        for (int i = 0; i < positions.size(); i++) {
            PositionData pos = positions.get(i);
            if (position.getX() == pos.getX() && position.getY() == pos.getY() && position.getZ() == pos.getZ()) {
                positions.remove(i);
                return;
            }
        }
        savePositions();
    }

    public static List<PositionData> getAllPositionNotIncluding(PositionData position) {
        List<PositionData> positionsNotIncluding = new ArrayList<PositionData>();
        for (PositionData pos : positions) {
            if (position.getX() == pos.getX() && position.getY() == pos.getY() && position.getZ() == pos.getZ())
                continue;
            positionsNotIncluding.add(pos);
        }
        return positionsNotIncluding;
    }

    public List<PositionData> getPositions() {
        return positions;
    }

    public static void loadPositions(File file) {
        dataFile = file;
        if (!file.exists()) {
            savePositions();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            PositionData[] loadedPositions = gson.fromJson(reader, PositionData[].class);
            positions.clear();
            positions.addAll(Arrays.asList(loadedPositions));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePositions() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(positions.toArray(new PositionData[0]), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}