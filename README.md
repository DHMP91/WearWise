# Inspiration?
I’ve always wanted to build a native Android app that I’d actually use myself. 
Over time, I realized I needed a tool to log my clothing and track my outfits. 
While there are existing app doing just that, I decided to dive in blind and create my own to exercise what little creativity I have... 
I’m not particularly into fashion; I just wanted something custom built for that part of my life. 

FYI: If you notice any quirky animations or design choices... it's a feature not a bug. It’s a personal project, why not have some fun with it?

# MyApp
An Android app that helps you organize and track your clothing items, build outfits, and manage your wardrobe effortlessly.

Features:
- Simple list view to display clothing items and outfits.
- Capture clothing or outfit photos using the built-in camera, or import images from your photo library.
- Blur backgrounds or crop images for a cleaner look.
- Build outfits based on your inventory of clothing.
- Track which outfits include specific clothing items.
- Easily filter through the list of clothing items to find what you're looking for.
- Use AI to autofill fields for quicker clothing logging.

## v2 AI Integration for Autocomplete
https://github.com/user-attachments/assets/6e6711c8-0c7f-4b8c-81b7-7dc37f8f8392

<img src="https://github.com/user-attachments/assets/5179f7e3-0cfd-483b-a62d-6ce23d854506" width="300">

User can now leverage AI to make the autocomplete feature much more powerful. 
Performance of autocomplete is highly dependent on configured AI and AI models. 


## Screenshots for clothing management
<p align="left">
  <img src="https://github.com/user-attachments/assets/8ed8140c-beca-4704-acf0-fa7124db1566" width="100" />
  <img src="https://github.com/user-attachments/assets/785257c1-6729-423c-a98b-b9705865ccdc" width="100" />
  <img src="https://github.com/user-attachments/assets/ad212343-8a90-4d97-bf62-d44222023793" width="100" />
</p>

## Screenshots for Outfit Management
<p align="left">
  <img src="https://github.com/user-attachments/assets/25d0989a-0879-47ca-8b66-4159d773d7d4" width="100" />
  <img src="https://github.com/user-attachments/assets/c6618d22-1817-426c-86fe-0dbd5619b037" width="100" />
</p>

## Screenshots for User Overview of Clothing Items and Outfits
<p align="left">
  <img src="https://github.com/user-attachments/assets/8fb195b1-970e-4b15-a71b-85d998ae5ff2" width="100" />
</p>


## Tech
- Framework/ToolKit: Jetpack Compose
- Language: Kotlin
- Database: SQLLite
- Persistence layer: Room DAO
- Test: Junit4, Mockito(Kotlin), Compose Test Rules, Compose Preview Screenshot Testing 
- Others: Google MLKit, CameraX
- AI Integration: Google Gemini, OpenAI (Coming soon)
