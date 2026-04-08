import openpyxl

wb = openpyxl.load_workbook('input/Salary Statement - 2025.xlsx', data_only=True)
sheet = wb.active

with open('excel_headers.txt', 'w', encoding='utf-8') as f:
    for row in range(1, 6):
        values = [str(cell.value) if cell.value is not None else "" for cell in sheet[row]]
        f.write(f"=== ROW {row} ===\n")
        out = []
        for i, v in enumerate(values):
            if v.strip(): out.append(f"{i}: {v}")
        f.write(" | ".join(out) + "\n")
