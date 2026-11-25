import sys
from px64 import sheet
from icons64_b import I
out = sys.argv[1]; icons = {k: f().render() for k, f in I.items()}
import os; os.makedirs(out, exist_ok=True)
for k, im in icons.items():
    im.save(f"{out}/{k.replace(' ', '_')}.png"); im.resize((128, 128), 0).save(f"{out}/{k.replace(' ', '_')}@2x.png")
sheet(icons, f"{out}/_sheet.png", scale=3, cols=6)
