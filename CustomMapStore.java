import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// เก็บ / โหลด / ลบ ด่านที่ผู้เล่นสร้างเอง (เซฟเป็นไฟล์ในโฟลเดอร์ custom_levels)
// รูปแบบไฟล์: บรรทัดแรก = ชื่อด่าน, บรรทัดถัดไป = แผนที่ (w = กำแพง, t = เป้าหมาย, b = กล่อง, p = ผู้เล่น)
public class CustomMapStore {

    public static final int MAX_MAPS = 5;
    private static final Path DIR = Paths.get(System.getProperty("user.home"), "NekoPawRescue", "custom_levels");

    // ข้อมูลด่าน 1 ด่าน
    public static class Entry {
        public final int slot;           // 1 - 5
        public final String name;
        public final List<String> lines; // แผนที่

        Entry(int slot, String name, List<String> lines) {
            this.slot = slot;
            this.name = name;
            this.lines = lines;
        }
    }

    private static Path fileOf(int slot) {
        return DIR.resolve("custom" + slot + ".txt");
    }

    // โหลดด่านทั้งหมดที่มี (เรียงตามช่อง)
    public static List<Entry> loadAll() {
        List<Entry> result = new ArrayList<>();
        for (int slot = 1; slot <= MAX_MAPS; slot++) {
            Path file = fileOf(slot);
            if (!Files.exists(file)) continue;
            try {
                List<String> all = Files.readAllLines(file, StandardCharsets.UTF_8);
                if (all.size() < 2) continue;
                result.add(new Entry(slot, all.get(0), new ArrayList<>(all.subList(1, all.size()))));
            } catch (IOException e) {
                System.err.println("Cannot read custom map " + file + ": " + e.getMessage());
            }
        }
        return result;
    }

    // โฟลเดอร์ที่เก็บแมพที่สร้างเอง (ใช้แสดงในข้อความเตือน)
    public static String folder() {
        return DIR.toAbsolutePath().toString();
    }

    public static int count() {
        return loadAll().size();
    }

    public static boolean isFull() {
        return count() >= MAX_MAPS;
    }

    // ช่องว่างช่องแรก (ใช้ตั้งชื่อเริ่มต้น) / -1 ถ้าเต็ม
    public static int nextFreeSlot() {
        for (int slot = 1; slot <= MAX_MAPS; slot++) {
            if (!Files.exists(fileOf(slot))) return slot;
        }
        return -1;
    }

    // เพิ่มด่านใหม่ คืน false ถ้าเต็มหรือเซฟไม่ได้
    public static boolean add(String name, List<String> lines) {
        int slot = nextFreeSlot();
        if (slot == -1) return false;

        List<String> out = new ArrayList<>();
        out.add(name);
        out.addAll(lines);
        try {
            Files.createDirectories(DIR);
            Files.write(fileOf(slot), out, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            System.err.println("Cannot save custom map: " + e.getMessage());
            return false;
        }
    }

    public static void delete(int slot) {
        try {
            Files.deleteIfExists(fileOf(slot));
        } catch (IOException e) {
            System.err.println("Cannot delete custom map: " + e.getMessage());
        }
    }
}
