## Overview
Overview of project— what problem are you solving, who is the client or target audience, and what is the plan moving forward for the project (after this class is over)

This application aims to increase visibility and accessibility of court cases, arguments, and decisions. Currently, users are able to select a given case (which are organized by year argued) and ask anything they might want about the case in plain english. In the future, I hope to add support for court opinions and dynamically downloading cases.
The target audience for this project is anyone interested in the supreme court, US government, or democratic processes.

## Database description
Database description— describe the tables in your database

In lieu of using a database, this project uses local storage. This is primarily because langchain4j supports serializing embedding stores to either json or .store files, both of which are tricky to store in a spring database. Currently, supported cases have been scrapped semi-manually.
I wrote a separate [node web scraping api](https://github.com/Poly1581/SCOTUSAPI/blob/main/api.js), which I used to download a years worth of case transcripts at a time (code available [here](/downloadTranscripts/downloadCases.js)) until I had 10 years worth of cases (2014-2024).
After this, transcript pdfs were preprocessed into in memory embedding stores and serialized to their respective embedding files (code available [here](/generateEmbeddings/src/main/java/org/example/Main.java)). When a user requests to interact with a given case (via the /chat get route), the server loads the corresponding embedding store,
makes an assistant with content retrieval from the embedding store, then queries the assistant with the given user prompt. In future versions, I hope to integrate the web scraping / transcript downloading into the backend in order to dynamically download whichever transcripts might not be already processed.


## AI Description
AI description— describe how Gen-AI is used in your application.

The application generates a new assistant for each case that a given user wants to interact with. Each time a user prompts the rag, the system checks if an assistant exists for that user and case, generates the rag if it does not exist yet, then prompts the assistant with the chat message from the user.
The assistant is given the system prompt "You are a constitutional law professor explaining supreme court cases. Respond concisely, correctly, and accurately." to help the system avoid excessively long and wordy responses.

## How to run/deploy
Deploying is a little tricky. When trying to deploy to google cloud, any time that a embedding store was loaded java instantly ran out of memory. Unfortunately, I was unable to figure out why the error was happening and therefore unable to deploy to google cloud. Instead, I am currently self hosting the backend on my home computer.
To host the backend like I am, there are a few steps. First, set up port forwarding for port 80 in your router (this step varies by internet company / router). Then, modify the firewall rules for your computer to allow connections on port 80 (this varies by operating system). Then, install [maven](https://maven.apache.org/download.cgi).
Finally, navigate to the [backend folder](/Backend) and run `mvn spring-boot:run` to start the server up.

## Video of app
A video of the app working is available [here](https://go.screenpal.com/watch/cZl1YunnYj2?_gl=1*11yon26*_ga*ODkwMzUwODA2LjE3MzM1MTY0ODA.*_ga_J7G603GGVL*MTczMzUxNjQ4MC4xLjEuMTczMzUxNzA3MC4wLjAuMA..).
