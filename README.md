# 🍉 Tangmo Game

เกม Java Swing ที่ผสมสองแนวเข้าด้วยกัน
- **Classic mode:** วางผลไม้ลงกล่อง ผลไม้ชนิดเดียวกันรวมกันเป็นผลไม้ใหญ่ขึ้น จนได้แตงโม (ฟิสิกส์วงกลมเขียนเอง)
- **Story mode:** เดินบนแผนที่ overworld เลือกด่าน รวมผลไม้เพื่อชาร์จพลัง แล้วไปสู้บอสแบบ Cuphead (ยิงเมล็ด กระโดด หลบกระสุน)

## Features
- 4 ด่าน แต่ละด่านมีแผนที่ อุปสรรค และบอสต่างกัน
- เมนู pause (เล่นต่อ / เริ่มใหม่ / กลับแผนที่ / กลับหน้าหลัก)
- Fullscreen (F11 / Alt+Enter)
- เสียง SFX เปลี่ยนเองได้โดยแทนไฟล์ .wav ในโฟลเดอร์ `sounds/`

## Run
Requires Java 21+
    ./run.sh

## Project Structure
main · core · scene · model (entity, attack, object) · map · ui · util

## Design Patterns
Scene pattern, Strategy (boss attacks)
