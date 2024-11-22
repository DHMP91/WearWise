# Test Automation Overview
All the test can be found under here. The directory structure mimics the app code structure to easily locate and pinpoint tests. 
![image](https://github.com/user-attachments/assets/a9ad6ea6-8b70-493d-86b6-ed964ee340b9)


# E2E UI Functional Test (UI Drived Test)
Library: Compose Testing (junit4, test manifest, test rules)

Covers: Composable functions
The E2E can be found under src/Screens/... The test class and file name is suffixed with "ScreenTest"

The UI test will focuses on the happy paths of user workflow. Mocking used in this layer is when necessary (authentication, 3rd party communicationm etc). 

# UI Layout Test (Screenshot based test)
Library: Compouse UI tooling preview

Covers: Screens Rendering

The screenshot test class can be found under src/screenshotTest and the screenshot is found under src/debug/screenshotTest

The UI layout test is use purely for layout changes. These test should not be used to verify a functionality of a product. 


To Run the test setup a run config:
![image](https://github.com/user-attachments/assets/68a46585-f7a5-4431-84fe-0132c91591a6)

To Update the screenshot setup config like

WARNING: Running this will update ALL screenshot. Verify the test result carefully before doing so.
![image](https://github.com/user-attachments/assets/24b52d21-fb3c-4892-aa60-3bce34f6240f)

# Unit Test
Libary: Junit4, Mockito

Cover: Repository code, models, view models

This layer heavily uses mocking of external and internal dependency. Exercising a portion or a function within the app code.
