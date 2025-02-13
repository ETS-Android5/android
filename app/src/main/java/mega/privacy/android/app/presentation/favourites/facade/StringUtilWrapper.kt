package mega.privacy.android.app.presentation.favourites.facade

/**
 * The interface for wrapping the static method regarding get string
 */
interface StringUtilWrapper {
    /**
     * Wrapping getSizeString function
     * @return size string
     */
    fun getSizeString(size: Long): String

    /**
     * Wrapping formatLongDateTime function
     * @return long date time string
     */
    fun formatLongDateTime(timestamp: Long): String

    /**
     * Get the folder information
     * @param numChildFiles child files number of current folder
     * @param numChildFolders child folders number of current folder
     */
    fun getFolderInfo(numChildFolders: Int, numChildFiles: Int): String
}