# SignYourWay
## Overview

Sign Your Way is a mobile application designed to facilitate communication through sign language recognition. Developed using **Java** and **Android Studio**, this app leverages a sophisticated machine learning model trained specifically for interpreting sign language into text. The goal is to bridge the gap between individuals who communicate through sign language and those who do not, making everyday interactions more accessible.

## Demo Video

Watch the demo video below to see the SignLanguageApp in action:

<iframe src="./Demo/Demo.mp4" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

---

Please note that the video quality may vary depending on your network speed and the viewer's device.

## Features

- **Sign to Text Translation:** Instantly translates sign language gestures into text using our pretrained model, facilitating seamless communication.
- **Text to Sign Animation:** Converts text inputs into visual sign language animations performed by an avatar, enhancing understanding and learning.
- **Interactive Learning:** Engage in a fun quiz mode to learn sign language. Achieve a high score to unlock the finale phase focusing on alphabet signs.
- **Comprehensive Dictionary:** Search for words related to specific topics in the dictionary. View images showing how each word is signed in sign language.
- **User-friendly Interface:** A clean and intuitive interface ensures easy navigation and usage.

## Machine Learning Model

At the heart of Sign Your Way lies a meticulously trained machine learning model, specifically designed for sign language interpretation. This model has been extensively trained on a large dataset of sign language gestures, enabling it to recognize a wide range of signs with high accuracy. The model's architecture is optimized for real-time processing, ensuring smooth and responsive performance even on mobile devices.

The development of this model involved several key steps:

### Model Architecture

- **Data Processing:** Initial data processing and preparation.
- **Model Selection and Training:** Selection and training of the Xception model pre-trained on ImageNet, customized for our specific task.
- **Model Evaluation:** Rigorous testing to ensure high accuracy and reliability.
- **Deployment:** Conversion to TensorFlow Lite format and integration into the Android app.

### Database Acquisition

The database includes 87,000 images, each 200x200 pixels in size, divided into 29 classes. Twenty-six of these classes correspond to the letters A-Z, and the remaining three classes are for Space, Delete, and Nothing.

### Data Preprocessing

- **Creation of Data Frame:** Organizing the data for efficient processing.
- **Image Normalization:** Rescaling pixel values for consistency.
- **Image Augmentation:** Generating additional training examples through transformations.
- **Data Generator:** Efficient data handling and management during training.

### Modeling and Evaluation

- **Based on Xception pre-trained on ImageNet** with specific modifications for our task.
- **Architecture:**
  1. Batch Normalization Layer.
  2. Dense Layer with 256 Units.
  3. Dropout Layer for regularization.
  4. Output Layer with 29 Units for the 29 classification classes.

### Model Deployment

- **Trained Model:** Finalized model ready for deployment.
- **Conversion to TFLITE:** Optimizing the model for efficient execution on mobile devices.
- **Deployment on Android:** Integrating the model into the SignLanguageApp for real-world use.
  
This model represents a significant advancement in the field of sign language recognition, offering users a reliable and intuitive tool for communication.

## Getting Started

To get started with this project, ensure you have the following prerequisites:

- Java Development Kit (JDK) installed on your system.
- Android Studio installed on your computer.
- Basic knowledge of Java and Android development.

### Installation

1. Clone this repository to your local machine:
   https://github.com/GuesmiGhassen/Sign-Your-Way-Application.git
   
3. Open the project in Android Studio:
   - Launch Android Studio.
   - Click on "Open an existing Android Studio project" and navigate to the cloned repository folder.
   - Select the `build.gradle` file located in the root directory of the project and click "OK".

4. Configure the project:
   - Ensure you have the necessary SDKs and build tools installed in Android Studio.
   - Run `./gradlew assembleDebug` from the terminal within the project directory to build the project.

5. Install the APK on a device:
   - Connect a device to your computer via USB or select an emulator from Android Studio.
   - Now you can explore the app and test its features.
