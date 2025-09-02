# SpeakGPT

<img src="https://assistant.teslasoft.org/SPEAKGPT_BANNER_ANDROID.png" style="width: 100%;"/>

SpeakGPT is an advanced and highly intuitive open-source AI assistant that utilizes the powerful large language models (LLM) to provide you with unparalleled performance and functionality. Officially it supports GPT models, LLAMA, MIXTRAL, GEMMA, Gemini (regular and pro) Vision, DALL-E and other models.

> [!NOTE]
> 
> This project is a part of my Bachelor Thesis. Attribution is required to use this work. Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
>
> Cite as: Dmytro Ostapenko (2024), "Review Program Automation Using Copilot Services" Bachelor Thesis, Technical University of Košice, 2024.


> [!CAUTION]
>
> We are dropping support of the following Android versions soon: 9, 10, 11. It's related with recent changes in SDK and security. Older Android versions uses deprecated and unstable features like RenderScript.


## Download

<a href = "https://play.google.com/store/apps/details?id=org.teslasoft.assistant"><img src="play.webp" alt="Get it on Play" width="200"/></a>

## SpeakGPT Web

Launch SpeakGPT Web: [https://assistant.teslasoft.org/](https://assistant.teslasoft.org/)

GitHub Repo: [Click here](https://github.com/AndraxDev/speak-gpt-web)

## Screenshots

<div align = "center">
	<img src="https://gpt.teslasoft.org/s/1.png" width="200"/>
	<img src="https://gpt.teslasoft.org/s/2.png" width="200"/>
	<img src="https://gpt.teslasoft.org/s/3.png" width="200"/>
</div>
<div align = "center">
	<img src="https://gpt.teslasoft.org/s/4.png" width="200"/>
	<img src="https://gpt.teslasoft.org/s/5.png" width="200"/>
	<img src="https://gpt.teslasoft.org/s/6.png" width="200"/>
</div>

## Information for users who want to use Google Gemini models with this app.

SpeakGPT does not support Google API keys itself, but you cen still use Google Gemini using OpenRouter API. More info at [OpenRouter Models](https://openrouter.ai/docs#models).

## For those not-far people who want to use something for free making low or no effort.

> [!WARNING]
> 
> Remember that free cheese could be only in a mousetrap. THIS APP IS OPEN-SOURCE CLIENT PROVIDED AS IS. ITSELF IT DOES NOT PROVIDE COMPLETELY FREE ACCESS TO THE PREMIUM FEATURES OF API PROVIDERS (LIKE FLAGSHIP AI MODELS AND SPECIAL FEATURES). IF YOU COME HERE TO USE OTHER'S WORK FOR FREE AND WITHOUT A CREDIT, IT'S BETTER YOU SKIP THIS APP AND LOOK FOR SOMETHING ELSE. I WILL NOT RESPOND TO YOUR "INCORRECT API KEY, WHY THIS APP REDIRECTS ME TO THE EXTERNAL SITE FOR API KEY?" QUESTIONS. THANK YOU FOR UNDERSTANDING.
> All other adequate people are welcome.

## API providers supported

- OpenAI (Full support)
- GROQ (Partial support)
- Azure (Partial support)
- OpenRouter (Text generation only, tested with Gemini, Claude, Perplexity, Llama, Gemma, Mistral, OpenAI models)
- Other (must be tested by community, don't be shy and provide your feedback)

> [!NOTE]
> 
> To change your API provider, go to settings and select the API endpoint. You can also add your custom API provider.

## Basic features

- [x] Chat (saved locally but can be imported/exported if needed)
- [x] Images generation
- [x] Image recognition (use your images and photos with ChatGPT)
- [x] Activation prompt
- [x] System message
- [x] Voice input (Whisper and Google)
- [x] Assistant
- [x] SpeakGPT in context menu
- [x] SpeakGPT in Share sheet
- [x] Function calling features
- [x] Prompts Library
- [x] Different chat layout
- [x] Adaptive design
- [x] A lot of different models
- [x] No captcha
- [x] Pay as you go system
- [x] Tips for newbies
- [x] Custom fine-tuned models are supported
- [x] AMOLED dark mode
- [x] Custom API provider support
- [x] Customize models params like temperature, topP, frequencyPenalty, presencePenalty and logit_bias
- [x] Playground
- [x] Access to the latest flagship models like o1, o3, o4, gpt-4.1, gpt-4.5 and gpt-image-1 (Some of these models may require you to verify your identity with OpenAI)

## ❌ Planned to add (Share your ideas in Issues)

- [ ] Device routines (like set alarm or open app)
- [ ] Sync chat history
- [ ] Add models exchange portal like prompts store
- [ ] Official browsing capabilities (make GPT AI models access the internet)

## API key safety:

SpeakGPT uses OpenAI API to provide you with the best experience. Using API-keys is more secure than using your username/password. Your personal info can't be obtained using API key. OpenAI provides cheap API access to their services. Your API key is stored locally on your device and is not shared with anyone. SpeakGPT does not collect any personal data. SpeakGPT is open-source and you can check the code yourself. Each release of SpeakGPT is checked on VirusTotal.
If you have any concerns you can secure either [revoke your API key](https://platform.openai.com/account/api-keys) or use a separate API key for SpeakGPT.

To secure your API key perform the following steps:

1. Make sure you have separate API key for SpeakGPT
2. Set up billing limit
3. Enable usage monitoring, so you can see how much resources SpeakGPT uses and how much it costs
4. If you have any concerns you can revoke your API key

> Why we obfuscate our code in production releases?
> 
> Obfuscation and resources shrinking allows us to optimize app size, it performance and secure it against reverse engineering or tamper and make sure your credentials like API keys in a safe place. You can request an unobfuscated build or compile it by self to make sure our app is safe.


> [!CAUTION]
>
> BE AWARE OF MALWARE! You are allowed to compile SpealGPT and modify it but be very careful when someone other offers you to install their build. Such build may contain malware. Official builds does not contain nay malware and are checked by more than 60 different antiviruses using VirusTotal. You can find VirusTotal report on each release page and compare the hash of the binary files.


> Developer identity
>
> Developer name: Dmytro Ostapenko (AndraxDev)\
> Contact: dostapenko82@gmail.com, +421951829517\
> Legal address: Južná trieda 4B, 04001 Košice, Slovakia 04001\
> Legal entity ID: 55545386 (D-U-N-S: 933739642)\
> License allowing performing commercial activity in Slovakia and EU: OU-KE-OZP1-2023/031005-2 (Issued on 14 June 2023 according to the § 10 section 1 letter a) of the Act No. 455/1991 Coll. on Trade Licensing (Trade Licensing Act) as amended)\
> VAT ID: SK3121636045\
> (So you know where you are sending your money if you decide to support the project financially or if project will have paid features in future)

## You are appreciated to:

- Report any bugs
- Support me :)
- Request new features. Don't forget to mark issue with a tag


## Buy me a coffee:

<a href="https://buymeacoffee.com/andrax_dev"><img src="https://andrax.dev/bmc_qr.png" width="200"/></a>

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/S6S6X3NCE)

## License

```
Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```