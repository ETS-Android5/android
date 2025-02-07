package mega.privacy.android.app.data.gateway.api

/**
 * MegaDBHandlerGateway gateway
 *
 * The gateway interface to the Mega DBhandler functionality
 */
interface MegaLocalStorageGateway {

    /**
     * Camera Uploads handle
     */
    suspend fun getCamSyncHandle(): Long?

    /**
     * Media Uploads handle
     */
    suspend fun getMegaHandleSecondaryFolder(): Long?
}