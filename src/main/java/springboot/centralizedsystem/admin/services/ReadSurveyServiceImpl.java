package springboot.centralizedsystem.admin.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class ReadSurveyServiceImpl implements ReadSurveyService {

    private int numberAddedRow = 0;

    private String getCellStringValue(Sheet sheet, int row, int column) {
        return sheet.getRow(row).getCell(column).getStringCellValue();
    }

    private int getMaxMergedRegionRowLength(Sheet sheet, List<CellRangeAddress> listMergedRegion) {
        int max = 0;
        int mergedRegionsSize = sheet.getNumMergedRegions();
        int rowRange = 0;
        CellRangeAddress cellRangeAddress = null;
        for (int i = 0; i < mergedRegionsSize; ++i) {
            cellRangeAddress = sheet.getMergedRegion(i);
            listMergedRegion.add(cellRangeAddress);
            rowRange = cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow() + 1;
            if (rowRange > max) {
                max = rowRange;
            }
        }
        return max;
    }

    private void sortMergedRegions(List<CellRangeAddress> listMergedRegion, boolean isSortByRow) {
        int size = listMergedRegion.size();
        for (int i = 0; i < size - 2; i++) {
            for (int j = size - 1; j > i; j--) {
                if (isSortByRow) {
                    if (listMergedRegion.get(j).getFirstRow() < listMergedRegion.get(j - 1).getFirstRow()) {
                        Collections.swap(listMergedRegion, j, j - 1);
                    }
                } else {
                    if (listMergedRegion.get(j).getFirstColumn() < listMergedRegion.get(j - 1).getFirstColumn()) {
                        Collections.swap(listMergedRegion, j, j - 1);
                    }
                }
            }
        }
    }

    private void addMergeRegionToJSON(Sheet sheet, int maxMergedRegionRowLength,
            List<CellRangeAddress> listMergedRegion, JsonObject jsonObject, int rowStartIndex,
            String parentStringCellValue) {
        List<CellRangeAddress> listMergedRegionTemp = new ArrayList<>();
        CellRangeAddress range = null;
        Cell cell = null;

        int regionsSize = listMergedRegion.size();
        if (regionsSize == 0) {
            numberAddedRow = rowStartIndex;
        }
        boolean isNotFirstRow = rowStartIndex != 0;

        for (int j = 0; j < regionsSize; j++) {
            range = listMergedRegion.get(j);
            int firstRow = range.getFirstRow();
            int firstColumn = range.getFirstColumn();

            // If row index != 0 -> must find parent cell
            if (isNotFirstRow) {
                cell = getParentCell(sheet, firstRow, firstColumn);
                if (cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                    parentStringCellValue = cell.getStringCellValue();
                }
            }

            if (firstRow == rowStartIndex) {
                if (range.getLastRow() == maxMergedRegionRowLength - 1) {
                    // If lastRow == maxMergedRegionRowLength -> no have child cell
                    if (isNotFirstRow) {
                        addKeyToJsonObject(jsonObject, parentStringCellValue,
                                getCellStringValue(sheet, firstRow, firstColumn), false);
                    } else {
                        jsonObject.addProperty(getCellStringValue(sheet, firstRow, firstColumn), "");
                    }
                } else {
                    if (isNotFirstRow) {
                        addKeyToJsonObject(jsonObject, parentStringCellValue,
                                getCellStringValue(sheet, firstRow, firstColumn), true);
                    } else {
                        jsonObject.add(getCellStringValue(sheet, firstRow, range.getFirstColumn()), new JsonObject());
                    }
                }
            } else {
                listMergedRegionTemp.add(range);
            }
        }

        rowStartIndex++;
        if (rowStartIndex < maxMergedRegionRowLength) {
            addMergeRegionToJSON(sheet, maxMergedRegionRowLength, listMergedRegionTemp, jsonObject, rowStartIndex,
                    parentStringCellValue);
        }
    }

    private Cell getParentCell(Sheet sheet, int currRowIndex, int currColIndex) {
        currRowIndex = currRowIndex - 1;
        Cell cell = sheet.getRow(currRowIndex).getCell(currColIndex);
        if (cell.getCellType() == Cell.CELL_TYPE_BLANK && currRowIndex > 0) {
            return getParentCell(sheet, currRowIndex, currColIndex);
        }
        return cell;
    }

    private void addKeyToJsonObject(JsonObject jsonObject, String parentKey, String currKey, boolean isAddJsonObject) {
        if (parentKey.equals("")) {
            if (isAddJsonObject) {
                jsonObject.add(currKey, new JsonObject());
            } else {
                jsonObject.addProperty(currKey, "");
            }
        } else {
            Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                String entryKey = entry.getKey();
                if (entryKey.equals(parentKey)) {
                    if (isAddJsonObject) {
                        jsonObject.getAsJsonObject(entryKey).add(currKey, new JsonObject());
                    } else {
                        jsonObject.getAsJsonObject(entryKey).addProperty(currKey, "");
                    }
                    break;
                }

                if (entry.getValue().isJsonObject()) {
                    addKeyToJsonObject(jsonObject.getAsJsonObject(entryKey), parentKey, currKey, isAddJsonObject);
                }
            }
        }
    }

    private boolean setValueToJSONBluePrint(JsonObject jsonObjectTemp, String value, int recursiveSteps) {
        Set<Entry<String, JsonElement>> entrySet = jsonObjectTemp.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            if (entry.getValue().isJsonObject()) {
                if (!setValueToJSONBluePrint(jsonObjectTemp.getAsJsonObject(entry.getKey()), value, ++recursiveSteps)) {
                    continue;
                } else if (--recursiveSteps == 0) {
                    break;
                } else {
                    return true;
                }
            } else {
                if (entry.getValue().getAsString().equals("")) {
                    jsonObjectTemp.addProperty(entry.getKey(), value);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> getListDataFromFile(String pathFile) throws IOException {
        FileInputStream file = new FileInputStream(new File(pathFile));

        // Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);

        // Get first/desired sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);

        // Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();

        // Prepare variable
        List<String> result = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        JsonObject jsonObject = new JsonObject();
        JsonObject jsonObjectTemp = null;
        JsonParser parser = new JsonParser();
        List<CellRangeAddress> listMergedRegion = new ArrayList<>();
        int maxMergedRegionRowLength = getMaxMergedRegionRowLength(sheet, listMergedRegion);
        Cell cell = null;
        Cell parentCell = null;
        String parentStringCellValue = "";
        String currStringCellValue = "";
        int currRowIndex = 0;
        int currColIndex = 0;
        boolean isReadHeader = true;
        String jsonBluePrint = ""; // Use to set value by key when read data after read header

        sortMergedRegions(listMergedRegion, true);
        sortMergedRegions(listMergedRegion, false);

        addMergeRegionToJSON(sheet, maxMergedRegionRowLength, listMergedRegion, jsonObject, 0, "");

        // Start reading
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            // For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();

            if (!jsonBluePrint.equals("")) {
                jsonObjectTemp = parser.parse(jsonBluePrint).getAsJsonObject();
            }

            while (cellIterator.hasNext()) {
                cell = cellIterator.next();
                if (isReadHeader) {
                    if (cell.getCellType() != Cell.CELL_TYPE_BLANK) {
                        // Reading header -> Cell always string
                        currStringCellValue = cell.getStringCellValue();
                        if (currStringCellValue.equals("(1)")) {
                            isReadHeader = false;
                            jsonBluePrint = jsonObject.toString();
                            break;
                        }

                        currRowIndex = cell.getRowIndex();
                        currColIndex = cell.getColumnIndex();
                        if (currRowIndex < numberAddedRow) {
                            // These cell in row was added
                            continue;
                        }
                        if (currRowIndex != 0) {
                            parentCell = getParentCell(sheet, currRowIndex, currColIndex);
                        }

                        if (!jsonObject.has(currStringCellValue)) {
                            if (parentCell != null && parentCell.getCellType() != Cell.CELL_TYPE_BLANK) {
                                parentStringCellValue = parentCell.getStringCellValue();
                            }
                            addKeyToJsonObject(jsonObject, parentStringCellValue, currStringCellValue, false);
                        }
                    }
                } else {
                    setValueToJSONBluePrint(jsonObjectTemp, formatter.formatCellValue(cell), 0);
                }
            }

            if (jsonObjectTemp != null) {
                result.add(jsonObjectTemp.toString());
            }
        }
        file.close();

        return result;
    }

    @Override
    public String getPathFileImport(File uploadRootDir, MultipartFile[] fileDatas) throws IOException {
        List<File> uploadedFiles = new ArrayList<>();

        for (MultipartFile fileData : fileDatas) {
            // Original file name at Client.
            String name = fileData.getOriginalFilename();

            if (name != null && name.length() > 0) {
                // Create file at Server.
                File serverFile = new File(uploadRootDir.getAbsolutePath() + File.separator + name);

                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                stream.write(fileData.getBytes());
                stream.close();

                uploadedFiles.add(serverFile);

                return serverFile.getAbsolutePath();
            }
        }

        return "";
    }
}
