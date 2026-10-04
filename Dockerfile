FROM eclipse-temurin:21-jdk

WORKDIR /app

# Libraries required by JavaFX/X11
RUN apt-get update && apt-get install -y \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgtk-3-0 \
    libgl1 \
    libglx0 \
    mesa-utils \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Download JavaFX SDK
RUN mkdir -p /javafx-sdk \
    && wget -O /tmp/javafx.zip \
    https://download2.gluonhq.com/openjfx/21/openjfx-21_linux-x64_bin-sdk.zip \
    && unzip /tmp/javafx.zip -d /javafx-sdk \
    && mv /javafx-sdk/javafx-sdk-21/lib /javafx-sdk/lib \
    && rm -rf /javafx-sdk/javafx-sdk-21 /tmp/javafx.zip

# Application classes
COPY target/classes /app/classes

# Maven runtime dependencies
COPY target/lib /app/lib

# X11 display
ENV DISPLAY=host.docker.internal:0.0

# Start JavaFX application
CMD ["java", \
    "--module-path", "/javafx-sdk/lib", \
    "--add-modules", "javafx.controls,javafx.fxml", \
    "-Dprism.order=sw", \
    "-cp", "/app/classes:/app/lib/*", \
    "app.Main"]
