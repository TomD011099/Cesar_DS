package Server.Util;

/**
 * This class is used for calculating the hash value
 */
public class CesarString {
    private String string;      //A CesarString is basically a regular string, we only override 3 methods

    /**
     * The constructor
     *
     * @param name The string
     */
    public CesarString(String name) {
        this.string = name;
    }

    /**
     * Get the string
     *
     * @return the string
     */
    public String getString() {
        return string;
    }

    /**
     * Assign the string
     *
     * @param name the new value
     */
    public void setString(String name) {
        this.string = name;
    }

    /**
     * Override for equals, to make sure the comparision is done as CesarStrings
     *
     * @param o The object to compare to
     * @return Does the object have the same hash?
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CesarString cesarString = (CesarString) o;

        return string.equals(cesarString.string);
    }

    /**
     * Calculate the hashcode, this is the reason we ude the override function.
     *
     * @return A value between 0 and 32768
     */
    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < string.length(); i++) {
            hash = hash + (int) Math.pow(string.charAt(i), i);
        }
        while ((hash < 0) || (hash > 32768)) {
            if (hash < 0) {
                hash += 32768;
            } else {
                hash -= 32768;
            }
        }
        return hash;
    }

    /**
     * For implicitly printing a CesarString
     *
     * @return the internal string
     */
    @Override
    public String toString() {
        return string;
    }

    /**
     * Get the length of the internal string
     *
     * @return the length of the internal string
     */
    public int length() {
        return string.length();
    }

    /**
     * For substringing a CesarSting
     *
     * @param beginIndex The index at which the returning string has to start
     * @param endIndex   The index at which the returning string has to end
     * @return The returning string
     */
    public String subString(int beginIndex, int endIndex) {
        return string.substring(beginIndex, endIndex);
    }
}