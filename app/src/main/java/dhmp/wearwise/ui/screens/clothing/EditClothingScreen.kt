package dhmp.wearwise.ui.screens.clothing

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dhmp.wearwise.R
import dhmp.wearwise.ui.AppViewModelProvider


@Composable
fun EditClothingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    garmentId: Long,
    clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory),
    context: Context = LocalContext.current
){
    clothingViewModel.getGarmentById(garmentId)
    val uiState by clothingViewModel.uiEditState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 5.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(uiState.editGarment.image).build(),
            contentDescription = "icon",
            contentScale = ContentScale.Inside,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max=300.dp)
                .padding(10.dp, 10.dp, 10.dp, 10.dp)
                .align(Alignment.CenterHorizontally)
        )
        Text(text="Inventory Item #$garmentId")
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ){

            Save()
            RemoveBackground(garmentId, uiState.editGarment.image)
//            AnalyzeGarment(garmentId)
            DeleteGarment(onFinish, garmentId)
        }

        ClothingInfo(garmentId)

    }
}

@Composable
fun RemoveBackground(garmentId: Long, image: String?, clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
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
fun DeleteGarment(onFinish: ()-> Unit, garmentId: Long, clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val isProcessing by clothingViewModel.isProcessingBackground.collectAsState()
    if(!isProcessing){
        Button(
            onClick = {
                clothingViewModel.deleteGarment(garmentId)
                onFinish()
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.errorContainer),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.shadow_add),
                contentDescription = "Blurr Background",
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Delete",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}


@Composable
fun AnalyzeGarment(garmentId: Long, clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val isProcessing by clothingViewModel.isProcessingBackground.collectAsState()
    if(!isProcessing){
        Button(
            onClick = {
                clothingViewModel.analyzeGarment(garmentId)
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSurface),
        ) {
            Text(
                text = "Analyze",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}


@Composable
fun Save(clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val uiState by clothingViewModel.uiEditState.collectAsState()
    uiState.changes?.let {
        Button(
            onClick = {
                clothingViewModel.saveChanges(it)
            },
            colors = ButtonDefaults.buttonColors(Color(0xFF77DD77)),
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

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingInfo(garmentId: Long, clothingViewModel: ClothingViewModel = viewModel(factory = AppViewModelProvider.Factory)){
    val uiState by clothingViewModel.uiEditState.collectAsState()
    val categories by clothingViewModel.categories.collectAsState()
    clothingViewModel.getGarmentById(garmentId)
    clothingViewModel.collectCategories()
    val garment = uiState.changes ?: uiState.editGarment

    val categoryNames = categories.map { it.name }
    val categoryId = garment.categoryId
    val category = categories.find { it.id == categoryId }
    val updateCategory = { name: String ->
        garment.categoryId = categories.find { it.name == name }?.id
        clothingViewModel.storeChanges(garment)
    }
    GarmentDropdownMenu("Category", categoryNames, category?.name, updateCategory, allowCustomValue = false)
    
//    DropUpMenu("A")
//    DropUpMenu("C")
//    DropUpMenu("K")
}

@Composable
fun GarmentDropdownMenu(label: String, options: List<String>, fieldValue: String?, updateField: (String) -> Unit, allowCustomValue: Boolean = true){
    var expanded by remember { mutableStateOf(false) }
    var expandedByFocus by remember { mutableStateOf(false) }
    var selectedText = fieldValue
    var textfieldSize by remember { mutableStateOf(Size.Zero) }
    val focusRequester = FocusRequester()
    val icon = if (expanded)
        Icons.Filled.KeyboardArrowDown
    else
        Icons.Filled.KeyboardArrowRight

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {},
        modifier = Modifier
            .width(with(LocalDensity.current){textfieldSize.width.toDp()}),
        properties = PopupProperties(focusable = false)
    ) {
        options.forEach { label ->
            DropdownMenuItem(
                text =  {
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
    OutlinedTextField(
        readOnly = allowCustomValue,
        value = selectedText ?: "",
        onValueChange = {
            updateField(it)
            selectedText = it
        },
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                //This value is used to assign to the DropDown the same width
                textfieldSize = coordinates.size.toSize()
            }
            .focusRequester(focusRequester)
            .onFocusChanged {
                if(!expanded && it.hasFocus && !expandedByFocus){
                    expanded = true
                    expandedByFocus = false
                } else if (expanded && !it.hasFocus){
                    expanded = false
                    expandedByFocus = false
                }
            },
        label = {Text(label)},
        trailingIcon = {
            Icon(
                icon,
                "contentDescription",
                Modifier.clickable {
                    expanded = !expanded
                    if(expanded){
                        focusRequester.requestFocus()
                    }
                }
            )
        }
    )

}