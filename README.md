# SpeakGPT

<img src="https://assistant.teslasoft.org/SPEAKGPT_BANNER_ANDROID.png" style="width: 100%;"/>

SpeakGPT is an advanced and highly intuitive open-source AI assistant that utilizes the powerful large language models (LLM) to provide you with unparalleled performance and functionality. Officially it supports GPT models, LLAMA, MIXTRAL, GEMMA, Gemini (regular and pro) Vision, DALL-E and other models.

> [!NOTE]
> 
> This project is a part of my Bachelor Thesis. Attribution is required to use this work. Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.
>
> Cite as: Dmytro Ostapenko (2024), "Review Program Automation Using Copilot Services" Bachelor Thesis, Technical University of Košice, 2024.

## Download

> [!WARNING]
> 
> Google Play page is deprecated as Google OCR could not read my apartment rental agreement and SpeakGPT may be removed from Google Play. Use "Releases" page to download the latest version.

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
- [x] Images generation (DALL-e)
- [x] GPT 4 Vision (use your images and photos with ChatGPT)
- [x] Activation prompt
- [x] System message
- [x] Voice input (Whisper and Google)
- [x] Assistant
- [x] SpeakGPT in context menu
- [x] SpeakGPT in Share sheet
- [x] Function calling features
- [x] Prompts store
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
- [x] GPT 4o

## ❌ Planned to add (Share your ideas in Issues)

- [ ] Device routines (like set alarm or open app)
- [ ] Sync chat history
- [ ] Add models exchange portal like prompts store

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
> Obfuscation and resources shrinking allows us to optimize app size, it performance and secure it against reverse engineering or tamper and make sure your credentials like API keys in a safe place.. You can request an unobfuscated build or compile it by self to make sure our app is safe.


> Developer identity
>
> Developer name: Dmytro Ostapenko (AndraxDev)\
> Contact: dostapenko82@gmail.com, +421951829517\
> Legal address: Južná trieda 4B, 04001 Košice, Slovakia 04001\
> Legal entity ID: 55545386

## You are appreciated to:

- Report any bugs
- Support me :)
- Request new features. Don't forget to mark issue with a tag


## Buy me a coffee:

<a href="https://buymeacoffee.com/andrax_dev"><img src="https://andrax.dev/bmc_qr.png" width="200"/></a>

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/S6S6X3NCE)

## License

```
Copyright (c) 2023-2024 Dmytro Ostapenko. All rights reserved.

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