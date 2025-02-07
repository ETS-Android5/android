package test.mega.privacy.android.app.data.mapper

import com.google.common.truth.Truth.assertThat
import mega.privacy.android.app.data.gateway.api.MegaApiGateway
import mega.privacy.android.app.data.mapper.toFavouriteInfo
import nz.mega.sdk.MegaNode
import org.junit.Test
import org.mockito.kotlin.mock

class FavouriteInfoMapperTest {

    @Test
    fun `test that values returned by gateway are used`() {

        val expectedId = 1L
        val expectedParentId = 2L
        val expectedBase64Id = "1L"
        val expectedModificationTime = 123L

        val node = mock<MegaNode> {
            on { handle }.thenReturn(expectedId)
            on { parentHandle }.thenReturn(expectedParentId)
            on { base64Handle }.thenReturn(expectedBase64Id)
            on { modificationTime }.thenReturn(expectedModificationTime)
        }
        val expectedHasVersion = true
        val expectedNumChildFolders = 2
        val expectedNumChildFiles = 3
        val gateway = mock<MegaApiGateway> {
            on { hasVersion(node) }.thenReturn(expectedHasVersion)
            on { getNumChildFolders(node) }.thenReturn(expectedNumChildFolders)
            on { getNumChildFiles(node) }.thenReturn(expectedNumChildFiles)
        }

        val actual = toFavouriteInfo(node, gateway)

        assertThat(actual.node).isSameInstanceAs(node)
        assertThat(actual.hasVersion).isEqualTo(expectedHasVersion)
        assertThat(actual.numChildFolders).isEqualTo(expectedNumChildFolders)
        assertThat(actual.numChildFiles).isEqualTo(expectedNumChildFiles)
        assertThat(actual.id).isEqualTo(expectedId)
        assertThat(actual.parentId).isEqualTo(expectedParentId)
        assertThat(actual.base64Id).isEqualTo(expectedBase64Id)
        assertThat(actual.modificationTime).isEqualTo(expectedModificationTime)
    }
}