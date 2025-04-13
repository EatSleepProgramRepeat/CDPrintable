# Contributing to CDPrintable

Thanks for your interest in helping out with **CDPrintable**! This project aims to make it easy to generate printable CD case inserts from track listings.

Whether you're fixing bugs, improving the UI, or adding features—your help is welcome!

---

## 🚀 Getting Started

1. **Fork this repository**  
   You’ll need your own copy of the repo to make changes.

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/CDPrintable.git
   cd CDPrintable
    ```
3. **Open in your IDE**  
   Open this project in IntelliJ IDEA or your preferred IDE. I use IntelliJ IDEA, but anything should work fine. The main entry point is `src/main/java/com.CDPrintable/main.java`.

## 🤝 Adding the original repo as a remote/rebasing

If you want to keep your fork up to date with the original repo, you can add it as a remote and rebase your changes on top of it.

1. Add the original repo as a remote:
    ```bash
   git remote add upstream https://github.com/EatSleepProgramRepeat/CDPrintable.git
    ```
2. Fetch the latest changes from the original repo:
    ```bash
    git fetch upstream
     ```
3. Merge the changes into your fork:
    ```bash
    git checkout main
    git merge upstream/main
    ```
4. Or, rebase to update your changes:
    ```bash
    git remote add upstream https://github.com/EatSleepProgramRepeat/CDPrintable.git
    git fetch upstream
    git rebase upstream/main
    ```
   
## 🧰 Ways to Contribute

1. 🐞 Report/Fix Bugs 
   - If you find a bug, please report it by opening an issue. If you can, try to fix it and submit a pull request.
   - If you want to fix a bug, please check the issues tab to see if it’s already been reported. If not, feel free to open a new issue.
2. 🎨 Improve the UI
   - If you have ideas for improving the UI, please open an issue or submit a pull request.
   - We are planning on (possibly) switching away from Swing. Let us know if you find anything better!
3. 🚀 Add Features
   - If you have ideas for new features, please open an issue or submit a pull request.
   - We are planning on adding a lot of new features, so feel free to jump in and help out!
4. ✅ Solve an issue
    - If you want to help out, please check the issues tab to see if there are any open issues. If you find one that you want to work on, please comment on it so that others know you’re working on it.
    - I like to make many detailed issues, so feel free to pick any of them!

## 📝 A Few Notes

- When you make a pull request, please reference the issue you’re working on in the description.
- Please make sure your code is well-documented and follows the existing style of the project.
- For more information, check out [Coding Conventions](https://github.com/EatSleepProgramRepeat/CDPrintable/wiki/Coding-Conventions).

# ❤️ Thank You!

We really appreciate anything you can do for this project. Thank you for your help on **CDPrintable**! You're making the world a better place by helping small open-source projects like this one.