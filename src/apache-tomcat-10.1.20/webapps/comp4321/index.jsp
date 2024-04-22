<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Search Engine</title>
    <link rel="stylesheet" href="styles.css">
    <style>
        :root {
            --color-bg: #ffffff; /* Background color */
            --color-text: #000000; /* Text color */
            --font-family: Arial, sans-serif; /* Font family */
            --line-height: 1.5; /* Line height */
        }
    </style>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="utils/utils.js"></script>
    <script src="utils/getData.js"></script>
</head>

<body>
    <header>
        <!-- add the image  -->
        <img src="images/logo.png" alt="COMP4321 Search Engine" style="width: 250px;">
        <!-- <h1>COMP4321 Search Engine</h1> -->
    </header>
    <main>
        <jsp:include page="html/inputForm.html" />

        <jsp:include page="html/searchResults.html" />
    </main>
    <footer>
        <p>COMP4321 Search Engine</p>
    </footer>
</body>
</html>
