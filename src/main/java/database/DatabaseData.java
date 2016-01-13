package database;

import java.util.Arrays;

public class DatabaseData {
    private String[] row;
    private int i = 0;

    public DatabaseData(String[] row) {
        this.row = row;
    }

    public String[] getRow() {
        return row;
    }

    public void add(String element){
        if (i < row.length){
            row[i] = element;
            i++;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseData that = (DatabaseData) o;

        return Arrays.equals(row, that.row);

    }

    @Override
    public int hashCode() {
        return row != null ? Arrays.hashCode(row) : 0;
    }
}
