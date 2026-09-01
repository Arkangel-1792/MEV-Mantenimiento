from pathlib import Path
import math
import sys
from PIL import Image, ImageDraw, ImageFont


def font(size: int):
    path = Path(r"C:\Windows\Fonts\arial.ttf")
    return ImageFont.truetype(str(path), size) if path.exists() else ImageFont.load_default()


def main() -> None:
    folder = Path(sys.argv[1])
    pages = sorted(folder.glob("page-*.png"), key=lambda p: int(p.stem.split("-")[-1]))
    out = folder / "contactos"
    out.mkdir(exist_ok=True)
    per_sheet = 4
    for start in range(0, len(pages), per_sheet):
        group = pages[start:start + per_sheet]
        opened = [Image.open(p).convert("RGB") for p in group]
        w = max(i.width for i in opened)
        h = max(i.height for i in opened)
        canvas = Image.new("RGB", (w * 2 + 60, h * 2 + 120), "#DCE2E8")
        draw = ImageDraw.Draw(canvas)
        for idx, (page_path, img) in enumerate(zip(group, opened)):
            col, row = idx % 2, idx // 2
            x, y = 20 + col * (w + 20), 50 + row * (h + 50)
            canvas.paste(img, (x, y))
            number = int(page_path.stem.split("-")[-1])
            draw.text((x, y - 36), f"Página {number}", font=font(26), fill="#17324D")
        sheet_no = start // per_sheet + 1
        canvas.save(out / f"contacto-{sheet_no:02d}.jpg", quality=88, optimize=True)


if __name__ == "__main__":
    main()
