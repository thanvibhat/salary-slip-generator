import openpyxl

path = 'input/Salary Statement - 2025.xlsx'
wb = openpyxl.load_workbook(path, data_only=True)
sheet = wb.active

print(f"Cell B1 value: '{sheet.cell(row=1, column=2).value}'")
