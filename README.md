# Project Name

## Setup Instructions

### Prerequisites

- Python 3.x installed
- Android Studio installed

### Configuration

1. **Firewall Settings:**
   - Disable the firewall to ensure proper communication with the Flask API.

2. **Network Configuration:**
   - Navigate to "Network & Internet" settings on Windows.
   - Change network profile to "Private" instead of "Public" to enable local network access.

### Running the Application

1. **Start Flask API:**
   - Place your model file in `Model/Classifier.py`.
   - Run the Flask API by executing the following command:
     ```
     flask run --host=0.0.0.0 --port=5000
     ```
   - This command makes the API accessible to all devices on the network.

2. **Run the Application:**
   - Start your application after the Flask API (model server) is up and running.

### Notes

- Ensure the Flask API is running (`flask run --host=0.0.0.0 --port=5000`) before launching the application.
- Adjust network and firewall settings as necessary for your environment.
