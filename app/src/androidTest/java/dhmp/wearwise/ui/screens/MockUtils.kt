package dhmp.wearwise.ui.screens

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dhmp.wearwise.model.Garment
import org.mockito.ArgumentCaptor


class FakePagingSource(
    private val garments: List<Garment>
) : PagingSource<Int, Garment>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Garment> {
        return LoadResult.Page(
            data = garments,
            prevKey = null,
            nextKey = null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Garment>) = null
}

fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()