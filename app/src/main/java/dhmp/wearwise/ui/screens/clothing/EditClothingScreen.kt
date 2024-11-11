package dhmp.wearwise.ui.screens.clothing

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dhmp.wearwise.R
import dhmp.wearwise.model.Category
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentColorNameTable
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.model.Occasion
import dhmp.wearwise.ui.AppViewModelProvider
import dhmp.wearwise.ui.screens.common.DropdownMenuEditable
import dhmp.wearwise.ui.screens.common.ScreenTitle
import dhmp.wearwise.ui.screens.common.TestTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch


@Composable
fun EditClothingScreen(
    onFinish: () -> Unit,
    onOutfits: (Long) -> Unit,
    onClickPicture: (String) -> Unit,
    onCrop: (String) -> Unit,
    onBack: () -> Unit,
    garmentId: Long,
    modifier: Modifier = Modifier,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
){
    var backPressHandled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = !backPressHandled) {
        println("back pressed")
        backPressHandled = true
        coroutineScope.launch(Dispatchers.Main) {
            awaitFrame()
            onBack()
            backPressHandled = false
        }
    }
    LaunchedEffect (garmentId) {
        clothingViewModel.getGarmentById(garmentId)
        clothingViewModel.getGarmentById(garmentId)
        clothingViewModel.collectBrands()
    }
    val uiState by clothingViewModel.uiEditState.collectAsState()
    val isProcessing by clothingViewModel.isProcessingBackground.collectAsState()
    val brands by clothingViewModel.brands.collectAsState()
    val isAnalyzing by clothingViewModel.analyzing.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp)
                .fillMaxSize()
                .weight(2f)
        ) {
            ClothingCard(
                uiState,
                brands = brands,
                isProcessing = isProcessing,
                isAnalyzing = isAnalyzing,
                onDelete = {
                    clothingViewModel.deleteGarment(garmentId)
                    onFinish()
                },
                onOutfits = onOutfits,
                onClickPicture = onClickPicture,
                onCrop = onCrop,
                garmentId = garmentId,
                onRemoveBackground = { p -> clothingViewModel.removeBackGround(garmentId, p)},
                onAnalyze = { clothingViewModel.analyzeGarment(garmentId) },
                onStoreChanges = { garment: Garment -> clothingViewModel.storeChanges(garment)}
            )
        }

        Row(
            modifier = Modifier
                .padding(
                    start = dimensionResource(id = R.dimen.screen_title_padding),
                    end = dimensionResource(id = R.dimen.screen_title_padding)
                )
                .weight(0.2f)
        ) {
            Save(uiState, saveChanges = {garment: Garment -> clothingViewModel.saveChanges(garment)})
        }
    }
}

@Composable
fun ClothingCard(
    uiState: EditClothingUIState,
    brands: List<String>,
    isProcessing: Boolean,
    isAnalyzing: Boolean,
    onDelete: () -> Unit,
    onOutfits: (Long) -> Unit,
    onClickPicture: (String) -> Unit,
    onCrop: (String) -> Unit,
    garmentId: Long,
    onRemoveBackground: (String) -> Unit,
    onAnalyze: () -> Unit,
    onStoreChanges: (Garment) -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        val pad = dimensionResource(id = R.dimen.screen_title_padding)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    start = pad,
                    top = dimensionResource(id = R.dimen.screen_title_padding),
                    end = pad
                )
        ) {
            Title(garmentId)
            DeleteGarment(
                onDelete = onDelete
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f)
        ) {
            GarmentImage(uiState.editGarment, onOutfits, onClickPicture, onCrop, isAnalyzing)
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(5.dp)
                .weight(0.75f),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RemoveBackground(uiState.editGarment.image, isProcessing, onRemoveBackground)
                AnalyzeGarment(isProcessing, isAnalyzing, onAnalyze = onAnalyze)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f)
        ) {
            ClothingInfo(uiState, brands, onStoreChanges)
        }

    }
}


@Composable
fun Title(
    garmentId: Long,
){
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
    ) {
        ScreenTitle("Clothing #${garmentId}")
    }
}

@Composable
fun DeleteGarment(
    onDelete: () -> Unit,
) {
    Box (
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            "Delete Clothing",
            modifier = Modifier
                .clickable {
                    onDelete()
                }
        )
    }

}

@Composable
fun GarmentImage(
    garment: Garment,
    onOutfits: (Long) -> Unit,
    onClickPicture: (String) -> Unit,
    onCrop: (String) -> Unit,
    isAnalyzing: Boolean,
    context: Context = LocalContext.current,
){
    Row(modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End,
        ){
            garment.color?.let {garmentColor ->
                val color = GarmentColorNameTable[garmentColor]
                color?.let {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.icon_max_height))  // Adjust the size as needed
                            .background(
                                Color(color.color),
                                shape = RoundedCornerShape(20.dp)
                            )
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (garment.image != null) {
                Box (
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                ){
                    var boxHeight by remember { mutableStateOf(0f) }
                    var boxWidth by remember { mutableStateOf(0f) }
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(garment.image).build(),
                        contentDescription = "GarmentImage",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .clickable {
                                garment.image?.let {
                                    onClickPicture(it)
                                }
                            }
                            .onGloballyPositioned { layoutCoordinates ->
                                // Capture the height of the Box
                                boxHeight = layoutCoordinates.size.height.toFloat()
                                boxWidth = layoutCoordinates.size.width.toFloat()
                            }
                    )
                    if(isAnalyzing) {
                        with(LocalDensity.current) {
                            ScanningAnimation(boxHeight.toDp(), boxWidth.toDp())
                        }
                    }
                }
            } else {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
                .padding(start = 5.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start,
        ){
            garment.image?.let {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .clickable { onCrop(it) }
                        .padding(
                            bottom = dimensionResource(id = R.dimen.screen_title_padding)
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.crop_icon),
                        contentDescription = "crop_image",
                        modifier = Modifier
                            .sizeIn(maxHeight = dimensionResource(R.dimen.icon_max_height))
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .clickable { onOutfits(garment.id) }
                    .padding(bottom = 5.dp)
                    .testTag(TestTag.OUTFIT_COUNT)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outfit),
                    contentDescription = "outfit_count",
                    modifier = Modifier
                        .sizeIn(maxHeight = dimensionResource(R.dimen.icon_max_height))
                )
                Text(
                    "${garment.outfitsId.size}"
                )
            }
        }
    }
}

@Composable
fun ScanningAnimation(targetHeight: Dp = 250.dp, scanWidth: Dp = 100.dp){
    val infiniteTransition = rememberInfiniteTransition(label = "transition")
    val verticalOffsetAnimation by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = targetHeight,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing, delayMillis = 500),
            repeatMode = RepeatMode.Restart
        ), label = "transition vertical"
    )

    val horizontalOffsetAnimation by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = scanWidth,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing, delayMillis = 500),
            repeatMode = RepeatMode.Restart
        ), label = "transition horizonal"
    )


    // Bottom to top scanner
    val startBottom = targetHeight - verticalOffsetAnimation
    VerticalScanLine(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if(startBottom > 10.dp) startBottom - 2.dp else startBottom),
        scanWidth
    )

    //Top to bottom scanner
    VerticalScanLine(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(1.dp)
            .padding(top = verticalOffsetAnimation),
        scanWidth
    )

    //Left to right scanner
    HorizontalScanLine(
        modifier = Modifier
            .fillMaxWidth()
            .width(1.dp)
            .padding(start = horizontalOffsetAnimation + 1.dp),
        targetHeight
    )

    //Right to left scanner
    val startRight = scanWidth - horizontalOffsetAnimation
    HorizontalScanLine(
        modifier = Modifier
            .fillMaxWidth()
            .width(1.dp)
            .padding(start = if(startRight > 10.dp) startRight - 1.dp else startRight),
        targetHeight
    )
}

@Composable
fun VerticalScanLine(modifier: Modifier, scanWidth: Dp){
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(scanWidth)
                .heightIn(3.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color = Color.Yellow)
                .graphicsLayer {
                    shadowElevation = 40.dp.toPx()
                    shape = RoundedCornerShape(10.dp)
                    clip = true
                }
                .background(color = Color(0x66FFFF00))
        )
    }
}

@Composable
fun HorizontalScanLine(modifier: Modifier, scanHeight: Dp){
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .heightIn(scanHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(color = Color.Yellow)
                .graphicsLayer {
                    shadowElevation = 40.dp.toPx()
                    shape = RoundedCornerShape(10.dp)
                    clip = true
                }
                .background(color = Color(0x66FFFF00))
        )
    }
}

@Composable
fun RemoveBackground(image: String?, isProcessing: Boolean, onRemoveBackground: (String) ->  Unit) {
    if(!isProcessing){
        Button(
            onClick = {
                image.let{
                    Uri.parse(it).path?.let { p -> onRemoveBackground(p) }
                }
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSurface),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.shadow_add),
                contentDescription = "Blurr Background",
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Blurr",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start=5.dp)
            )
        }
    }else {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}



@Composable
fun AnalyzeGarment(isProcessing: Boolean, isAnalyzing: Boolean, onAnalyze: () -> Unit) {
    if(!isProcessing){
        if(isAnalyzing){
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSurface),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = "Scanning",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }else {
            Button(
                onClick = { onAnalyze() },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSurface),
            ) {
                Text(
                    text = "Auto Complete",
                    style = MaterialTheme.typography.labelMedium,
                )

            }
        }
    }
}


@Composable
fun Save(uiState: EditClothingUIState, saveChanges: (Garment) -> Unit) {

    var buttonColor = ButtonDefaults.buttonColors(Color.Gray)
    var onClick = {}
    var isSaved = true
    uiState.changes?.let{
        buttonColor = ButtonDefaults.buttonColors(colorResource(R.color.accent))
        onClick = {
            saveChanges(it)
        }
        isSaved = false
    }
    Button(
        onClick = onClick,
        colors = buttonColor,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Save",
            color = if(isSaved) Color.White else Color.Black
        )
    }
}


@Composable
fun ClothingInfo(
    uiState: EditClothingUIState,
    brands: List<String>,
    onStoreChanges: (Garment) -> Unit
){
    val garment = uiState.changes ?: uiState.editGarment
    Column (
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        GarmentCategorySelection(garment, onStoreChanges)

        val colorNames = GarmentColorNames.map { it.name }
        val updateColor = { name: String ->
            garment.color = name
            onStoreChanges(garment)
        }
        dhmp.wearwise.ui.screens.common.DropdownMenu(
            "Color",
            colorNames,
            garment.color,
            updateColor
        )


        val updatebrand = { value: String ->
            garment.brand = value
            onStoreChanges(garment)
        }
        DropdownMenuEditable("Brand", brands, garment.brand, updatebrand)

        val updateOccasion = { value: String ->
            Occasion.valueOf(value)
            garment.occasion = Occasion.valueOf(value)
            onStoreChanges(garment)
        }
        val occasions = Occasion.entries.map { it.name }
        dhmp.wearwise.ui.screens.common.DropdownMenu(
            "Occasion",
            occasions,
            garment.occasion?.name,
            updateOccasion
        )
    }
}


@Composable
fun GarmentCategorySelection(
    garment: Garment,
    onStoreChanges: (Garment) -> Unit
){
    val categories = Category.categories()
    val categoryNames = categories.map { it.name }
    var category: Category? = null
    garment.categoryId?.let { categoryId ->
        category = Category.getCategory(categoryId)
    }
    val updateCategory = { name: String ->
        garment.categoryId = Category.getCategory(name)?.id
        onStoreChanges(garment)
    }

    if(category == null){
        dhmp.wearwise.ui.screens.common.DropdownMenu(
            "Category",
            categoryNames,
            null,
            updateCategory
        )
    }else {
        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 5.dp)
            ) {
                dhmp.wearwise.ui.screens.common.DropdownMenu(
                    "Category",
                    categoryNames,
                    category!!.name,
                    updateCategory
                )
            }

            val subCategoryNames = category!!.subCategories?.map { it.name }

            if (subCategoryNames != null) {
                var subCategory: Category? = null
                garment.subCategoryId?.let { subCategoryId ->
                    subCategory = category!!.subCategories?.find { subCategory -> subCategory.id == subCategoryId }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    val updateSubCategory = { name: String ->
                        garment.subCategoryId = Category.getCategory(name)?.id
                        onStoreChanges(garment)
                    }
                    dhmp.wearwise.ui.screens.common.DropdownMenu(
                        "SubCategory",
                        subCategoryNames,
                        subCategory?.name,
                        updateSubCategory
                    )
                }
            }
        }
    }
}


