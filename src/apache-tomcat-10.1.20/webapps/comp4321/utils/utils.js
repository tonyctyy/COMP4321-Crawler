function updateUI(response, useANDFlag = false) {
    // Clear the existing content of the data container
    $("#dataContainer").empty();
    $("#num-results").empty();

    // Get the pages from the response
    var pages = response.pages;

    // Get the order of the pages from the response
    var order = response.sortedPages;

    // Check if there are no results or display the number of results
    if (order.length === 0) {
        $("#num-results").append("<p style='color: red;'>No results found.</p>");
        return;
    } else {
        var totalPages = response.totalPage
        $("#num-results").append("<p>" + order.length + " relevant results.</p>");
    }

    // Calculate the number of tabs needed based on the total number of results
    var numTabs = Math.ceil(order.length / 5);

    // Create a container for the tabs
    var tabsContainer = $("<div class='tabs-container'></div>");

    // Loop through each tab
    for (var i = 0; i < numTabs; i++) {
        // Calculate the start and end indices for the current tab
        var startIndex = i * 5;
        var endIndex = Math.min(startIndex + 5, order.length);

        // Create a tab for the current subset of results
        var tabContent = $("<div class='tab-content'></div>");

        // Loop through the subset of results and create cards for each page
        for (var j = startIndex; j < endIndex; j++) {
            var page = pages[order[j]];
            var card = createCard(page);
            if (useANDFlag) {
                card.addClass('glowBGnoBlack');
            }
            tabContent.append(card);
        }

        // Append the tab content to the tabs container
        tabsContainer.append(tabContent);
    }

    // Append the tabs container to the data container
    $("#dataContainer").append(tabsContainer);

    // Create tab navigation buttons
    createTabNavigation(numTabs);
}

function createCard(page) {
    // Create a card for the page
    var card = $("<div class='result-card'></div>");

    // Left column for similarity score, round to 3 decimal places
    var leftColumn = $("<div class='left-column'></div>");
    leftColumn.append("<p>"+ page.similarity.toFixed(3) +"</p>");

    // Right column for page details
    var rightColumn = $("<div class='right-column'></div>");
    rightColumn.append("<h3>" + page.title + "</h3>");
    rightColumn.append("<p><a href='" + page.url + "'>" + page.url + "</a></p>");
    // Add the lastModificationDate and size in the same line to the right column 
    rightColumn.append("<p>" + page.lastModificationDate + ", " + page.size + " bytes</p>");
    // Add the five most common words to the right column (in the page.keyWords in a format of key-value pair where key is the word and value is the frequency) Add the words in the same line according to the frequency in descending order
    var keyWords = page.keyWords;
    var keyWordsString = "";
    var keyWordsArray = [];
    for (var key in keyWords) {
        keyWordsArray.push([key, keyWords[key]]);
    }
    keyWordsArray.sort(function(a, b) {
        return b[1] - a[1];
    });
    for (var j = 0; j < 5 && j < keyWordsArray.length; j++) {
        keyWordsString += keyWordsArray[j][0] + " | " + keyWordsArray[j][1] + ", ";
    }
    keyWordsString = keyWordsString.slice(0, -2);
    rightColumn.append("<p>" + keyWordsString + "</p>");

    // Add the parent link to the right column
    var parentPages = page.parentPages;
    // check if parentPage is null or undefined
    if (Object.keys(parentPages).length > 0){
        rightColumn.append("<h4>Parent Link(s):</h4>");
    }
    for (var key in parentPages) {
        var parentLink = parentPages[key];
        var paragraph = $("<p><a href='" + parentLink.url + "'>" + parentLink.title + "</a></p>");
        // Append the paragraph to the right column
        rightColumn.append(paragraph);
    }
    
    // Add the child links to the right column
    var childPages = page.childPages;
    var count = 0;
    var showMoreBtn = $("<button class='show-more-btn'>Show More</button>");
    if (Object.keys(childPages).length >0){
        rightColumn.append("<h4>Child Link(s):</h4>");
    }
    for (var key in childPages) {
        var childPage = childPages[key];
        var paragraph;
        if (count <5){
            paragraph = $("<p><a href='" + childPage.url + "'>" + childPage.title + "</a></p>");
        }
        else{
            paragraph = $("<p class='child-link' style='display: none;'><a href='" + childPage.url + "'>" + childPage.title + "</a></p>");
        }
        // Append the paragraph to the right column
        rightColumn.append(paragraph);
        count++;
        if (count == 5){
            rightColumn.append(showMoreBtn);
        }
    }

    // Append left and right columns to the card
    card.append(leftColumn);
    card.append(rightColumn);

    return card;
}


function createTabNavigation(numTabs) {
    // Create a container for the tab navigation buttons
    var tabNavContainer = $("<div class='tab-nav-container'></div>");

    // Loop through each tab and create a button for it
    for (var i = 0; i < numTabs; i++) {
        var tabBtn = $("<button class='tab-btn'></button>");
        tabBtn.text((i + 1));
        tabBtn.attr("data-tab-index", i);
        tabNavContainer.append(tabBtn);
    }

    // Append the tab navigation container to the data container
    $("#dataContainer").append(tabNavContainer);

    // Show the first tab content by default
    $(".tab-content").hide();
    $(".tab-content:nth-child(1)").show();

    // Add click event listeners to the tab buttons
    $(".tab-btn").click(function() {
        // Remove the active class from all buttons
        $(".tab-btn").removeClass("active");

        // Get the index of the clicked tab
        var tabIndex = $(this).attr("data-tab-index");

        // Show the corresponding tab content and hide others
        $(".tab-content").hide();
        $(".tab-content:nth-child(" + (parseInt(tabIndex) + 1) + ")").show();
        $(this).addClass("active");

        $("#search-container").scrollTop(0);
    });

    // Add active class to the first tab button by default
    $(".tab-btn:first-child").addClass("active");
}


// Attach event handler for "show more" button
$(document).on("click", ".show-more-btn", function() {
    // Toggle visibility of extra links
    $(this).siblings(".child-link:nth-child(n+6)").toggle();
    
    // Update button text based on visibility
    $(this).text($(this).text() === "Show More" ? "Show Less" : "Show More");
});

function updateMergedUI(mergedList) {
    // Clear the existing content of the data container
    $("#dataContainer").empty();

    let length = mergedList.length;

    // Calculate the number of tabs needed based on the total number of results
    var numTabs = Math.ceil(length / 5);

    // Create a container for the tabs
    var tabsContainer = $("<div class='tabs-container'></div>");

    // Loop through each tab
    for (var i = 0; i < numTabs; i++) {
        // Calculate the start and end indices for the current tab
        var startIndex = i * 5;
        var endIndex = Math.min(startIndex + 5, length);

        // Create a tab for the current subset of results
        var tabContent = $("<div class='tab-content'></div>");

        // Loop through the subset of results and create cards for each page
        for (var j = startIndex; j < endIndex; j++) {
            var page = getDataFromLocalStorage(mergedList[j][1]).pages[mergedList[j][0]];
            var card = createCard(page);
            if (mergedList[j][3] == null) {
                card.addClass('glowBGnoBlack');
            } else {
                card.css("background-color", mergedList[j][3]);
            }
            tabContent.append(card);
        }

        // Append the tab content to the tabs container
        tabsContainer.append(tabContent);
    }

    // Append the tabs container to the data container
    $("#dataContainer").append(tabsContainer);

    // Create tab navigation buttons
    createTabNavigation(numTabs);
}