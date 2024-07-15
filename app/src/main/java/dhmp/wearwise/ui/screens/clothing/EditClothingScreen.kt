package dhmp.wearwise.ui.screens.clothing

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dhmp.wearwise.R
import dhmp.wearwise.model.Garment
import dhmp.wearwise.model.GarmentColorNames
import dhmp.wearwise.model.Occasion
import dhmp.wearwise.ui.AppViewModelProvider


@Composable
fun EditClothingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    garmentId: Long,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
){
    LaunchedEffect (garmentId) {
        clothingViewModel.getGarmentById(garmentId)
    }
    val uiState by clothingViewModel.uiEditState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
            .padding(top = 5.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .weight(0.3f)
            .padding(top=10.dp, end = 20.dp)
        ) {
            DeleteGarment(onFinish, garmentId)
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .weight(2f)
        ) {
            GarmentImage(uiState.editGarment)
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(5.dp)
                .weight(0.5f),
            horizontalArrangement = Arrangement.Center
        ) {
            Row (
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RemoveBackground(garmentId, uiState.editGarment.image)
                AnalyzeGarment(garmentId)
                Save()
            }
        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .weight(4f)
        ) {
            ClothingInfo(garmentId)
        }
//        Row(
//            modifier = Modifier
//                .align(Alignment.CenterHorizontally),
//            horizontalArrangement = Arrangement.spacedBy(10.dp)
//        ){
//            Save()
//        }
    }
}

@Composable
fun DeleteGarment(
    onFinish: ()-> Unit,
    garmentId: Long,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)
) {
    Box (
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            "Delete",
            modifier = Modifier
                .clickable {
                    clothingViewModel.deleteGarment(garmentId)
                    onFinish()
                }
        )
    }
}

@Composable
fun GarmentImage(
    garment: Garment,
    context: Context = LocalContext.current,
){
    Row(modifier = Modifier
        .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ){

        }
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (garment.image != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(garment.image).build(),
                    contentDescription = "icon",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .heightIn(max=300.dp)
                )

            } else {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start,
        ){
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
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
fun RemoveBackground(garmentId: Long, image: String?, clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)) {
    val isProcessing by clothingViewModel.isProcessingBackground.collectAsState()
    if(!isProcessing){
        Button(
            onClick = {
                image.let{
                    Uri.parse(it).path?.let { p -> clothingViewModel.removeBackGround(garmentId, p) }
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
fun AnalyzeGarment(garmentId: Long, clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)) {
    val isProcessing by clothingViewModel.isProcessingBackground.collectAsState()
    if(!isProcessing){
        Button(
            onClick = {
                clothingViewModel.analyzeGarment(garmentId)
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSurface),
        ) {
            Text(
                text = "Auto Complete",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}


@Composable
fun Save(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)) {
    val uiState by clothingViewModel.uiEditState.collectAsState()
    var buttonColor = ButtonDefaults.buttonColors(Color.Gray)
    var onClick = {}
    uiState.changes?.let{
        buttonColor = ButtonDefaults.buttonColors(Color(0xFF77DD77))
        onClick = {
            clothingViewModel.saveChanges(it)
        }
    }

    Button(
        onClick = onClick,
        colors = buttonColor,
    ) {
        Icon(
            imageVector = Icons.Outlined.Done,
            contentDescription = "Save Changes",
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Save",
            style = MaterialTheme.typography.labelMedium,
        )
    }


}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingInfo(garmentId: Long, clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.ClothingFactory)){
    LaunchedEffect (garmentId) {
        clothingViewModel.getGarmentById(garmentId)
        clothingViewModel.collectCategories()
        clothingViewModel.collectBrands()
    }
    val uiState by clothingViewModel.uiEditState.collectAsState()
    val categories by clothingViewModel.categories.collectAsState()
    val brands by clothingViewModel.brands.collectAsState()
    val garment = uiState.changes ?: uiState.editGarment

    val categoryNames = categories.map { it.name }
    val categoryId = garment.categoryId
    val category = categories.find { it.id == categoryId }
    val updateCategory = { name: String ->
        garment.categoryId = categories.find { it.name == name }?.id
        clothingViewModel.storeChanges(garment)
    }
    GarmentDropdownMenu("Category", categoryNames, category?.name, updateCategory)

    val colorNames = GarmentColorNames.map { it.name }
    val updateColor = { name: String ->
        garment.color = name
        clothingViewModel.storeChanges(garment)
    }
    GarmentDropdownMenu("Color", colorNames, garment.color, updateColor)


    val updatebrand = { value: String ->
        garment.brand = value
        clothingViewModel.storeChanges(garment)
    }
    GarmentDropdownMenuCustom("Brand", brands, garment.brand, updatebrand)

    val updateOccasion = { value: String ->
        Occasion.valueOf(value)
        garment.occasion = Occasion.valueOf(value)
        clothingViewModel.storeChanges(garment)
    }
    val occasions = Occasion.entries.map { it.name }
    GarmentDropdownMenuCustom("Occasion", occasions, garment.occasion?.name, updateOccasion)
//    DropUpMenu("A")
//    DropUpMenu("C")
//    DropUpMenu("K")
}

@Composable
fun GarmentDropdownMenu(label: String, options: List<String>, fieldValue: String?, updateField: (String) -> Unit){
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = configuration.screenHeightDp.dp
    var expanded by remember { mutableStateOf(false) }
    var expandedByFocus by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }
    var staticSelectedText by remember { mutableStateOf( fieldValue ?: "")}
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val menuHeight = with(density) {
        (screenHeightPx / 4).toPx().toDp()
    }
    val focusRequester = FocusRequester()
    val icon = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            readOnly = true,
            value = if (staticSelectedText == "" && fieldValue != null) fieldValue else staticSelectedText,
            onValueChange = {
                updateField(it)
                staticSelectedText = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    //This value is used to assign to the DropDown the same width
                    textFieldSize = coordinates.size.toSize()
                }
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.hasFocus && !expanded && !expandedByFocus) {
                        //Expand once when focus on field (clicking directly)
                        expanded = true
                        expandedByFocus = true
                    }
                },
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    icon,
                    "contentDescription",
                    Modifier.clickable {
                        if(!dismissed){
                            expanded = !expanded
                        }else if(!expanded){
                            // Clickable is not called when readOnly (allowCustomValue = false)
                            expanded = true
                            dismissed = false //Reset, conflict event with closing + dismiss menu
                        }else{
                            dismissed = false
                        }
                    }
                )
            }
        )

        val textFieldHeightDp: Dp = with(density) { textFieldSize.height.toDp() }
        val textFieldWidthDp: Dp = with(density) { textFieldSize.width.toDp() }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                dismissed = true
            },
            offset = DpOffset(x = 0.dp, y = -menuHeight - textFieldHeightDp),
            modifier = Modifier
                .width(textFieldWidthDp)
                .height(menuHeight),
            properties = PopupProperties(focusable = true)
        ) {
            options.forEach { label ->
                DropdownMenuItem(
                    text = {
                        Text(text = label)
                    },
                    onClick = {
                        updateField(label)
                        staticSelectedText = label
                        expanded = false
                    }
                )
            }
        }
    }
}



@Composable
fun GarmentDropdownMenuCustom(label: String, options: List<String>, fieldValue: String?, updateField: (String) -> Unit){
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = configuration.screenHeightDp.dp
    var expanded by remember { mutableStateOf(false) }
    var expandedByFocus by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf( fieldValue?: "") }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val menuHeight = with(density) {
        (screenHeightPx / 4).toPx().toDp()
    }
    val focusRequester = FocusRequester()
    val icon = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = if (selectedText == "" && fieldValue != null) fieldValue else selectedText,
            onValueChange = {
                updateField(it)
                selectedText = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    //This value is used to assign to the DropDown the same width
                    textFieldSize = coordinates.size.toSize()
                }
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.hasFocus && !expanded && !expandedByFocus) {
                        //Expand once when focus on field (clicking directly)
                        expanded = true
                        expandedByFocus = true
                    } else if (!it.hasFocus && expanded) {
                        expanded = false
                        expandedByFocus = true
                    }
                },
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    icon,
                    "contentDescription",
                    Modifier.clickable {
                        if(!dismissed){
                            expanded = !expanded
                            if(expanded){
                                focusRequester.requestFocus()
                            }
                        }else{
                            dismissed = false
                        }
                    }
                )
            }
        )

        val textFieldHeightDp: Dp = with(density) { textFieldSize.height.toDp() }
        val textFieldWidthDp: Dp = with(density) { textFieldSize.width.toDp() }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                dismissed = true
            },
            offset = DpOffset(x = 0.dp, y = -textFieldHeightDp),
            modifier = Modifier
                .width(textFieldWidthDp)
                .heightIn(max = menuHeight),
            properties = PopupProperties(focusable = false)
        ) {
            options.forEach { label ->
                DropdownMenuItem(
                    text = {
                        Text(text = label)
                    },
                    onClick = {
                        updateField(label)
                        selectedText = label
                        expanded = false
                    }
                )
            }
        }
    }
}