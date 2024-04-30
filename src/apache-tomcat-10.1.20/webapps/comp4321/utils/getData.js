$(document).ready(function () {
    $("#searchForm").submit(function (event) {
        // Prevent the default form submission behavior
        event.preventDefault();
        var input = $("#searchInput").val();
        // Call your function or perform any other actions here
        let pageIDFilter = JSON.stringify(selectedPageIDList);
        console.log(pageIDFilter);
        console.log(selectedPageIDList.length);
        getPages(input, pageIDFilter, selectedPageIDList.length); // Call your function to fetch data
        resetSelectedPageID();
        resetBaseMergedList();

        addORToSearchSequence();
        addSearchToSearchSequence(input);
        displaySearchSequence();
        resetSearchSequence();
    });
    $("#removeLocalStorageButton").click(function() {
        // Clear the local storage
        localStorage.clear();

        // Optional: Update the UI or perform any other actions after removing the items
        console.log("Local storage cleared!");

        // Optional: Refresh the page or update any relevant UI elements
        location.reload();
        resetSelectedPageID();
        resetBaseMergedList();
        updateUI();

        resetSearchSequence();
        displaySearchSequence();
    });
    $("#UnselectButton").click(function() {
        // Unselect all checkboxes
        $("input[type='checkbox']").prop("checked", false);
        
        // Reload the page
        location.reload();
        resetSelectedPageID();
        resetBaseMergedList();

        resetSearchSequence();
        displaySearchSequence();
    });
    // init filter
    createChecklistForLocalStorageKeys();
});

function getPages(input, pageIDFilter, filterLen, useANDFlag = false) {
    $.ajax({
        url: "../comp4321/apis/getData.jsp", // Endpoint URL to fetch data
        data: { input: input , pageIDFilter: pageIDFilter, filterLen: filterLen}, // Data to be sent to the server
        type: "GET", // HTTP method
        dataType: "json", // Data type expected from the server
        success: function (data) {
            // Handle successful response
            // Process the data and update the UI as needed
            // console.log(typeof data);
            updateUI(data, useANDFlag); //in utils.js
            if (!useANDFlag) {
                storeDataInLocalStorage(input, data); // Store the data in local storage
                createChecklistForLocalStorageKeys();
            } else {
                selectedPageIDList = data.sortedPages;
                basePageIDSet = new Set(data.sortedPages);
                createChecklistForLocalStorageKeys(true);
                addToMergeList(input, data);
                //console.log(baseMergedList);
                console.log(basePageIDSet);
            }
        },
        error: function (xhr, status, error) {
            // Handle error response
            console.error(xhr.responseText); // Log the error message   
        }
    });
}

let selectedPageIDList = [];

let selectedPageID = new Set();

let basePageIDSet = new Set();

let selectedInputSet = new Set();

let baseMergedList = [];

let SearchSequence = [];

function resetBasePageIDSet() {
    basePageIDSet = new Set();
    createChecklistForLocalStorageKeys();
}

function resetBaseMergedList(){
    baseMergedList = [];
}

function addToMergeList(key, data) {
    for (var j = 0; j < data.sortedPages.length; j++) {
        const index = data.sortedPages[j];
        const page = data.pages[index];
        baseMergedList.push([index, key, page.similarity, null]);
    }
}

function resetSelectedPageID() {
    selectedPageID = new Set();
    selectedPageIDList = [];
}

function displaySearchSequence() {
    $(".SearchSequence").empty(); // Clear the existing content
    for (let i = 0; i < SearchSequence.length; i++) {
        //console.log(SearchSequence[i]); // Print to console
        $(".SearchSequence").append(SearchSequence[i] + " "); // Append to an HTML element
    }
}

function addBrackets() {
    SearchSequence.unshift("("); // Add opening parentheses at the beginning
    SearchSequence.push(")"); // Add closing parentheses at the end
}

function addORToSearchSequence() {
    if (selectedInputSet.size == 0) return;
    const myArray = Array.from(selectedInputSet);
    let string = " ";
    if (SearchSequence.length != 0)
        string += "+ ";
    string += "(";
    myArray.forEach((element, index) => {  
        string += element;
        if (index < myArray.length - 1) {
            string += " + ";
        }
    });
    string += ") ";
    SearchSequence.push(string);
}

function addSearchToSearchSequence(input) {
    let string = " ";
    if (SearchSequence.length != 0)
        string += "&rarr; "
    string += "Search: " + input;
    SearchSequence.push(string);
}

function addANDToSearchSequence(input) {
    let string = " ";
    if (SearchSequence.length != 0)
        string += "AND "
    string += input;
    SearchSequence.push(string);
}

function resetSearchSequence() {
    SearchSequence = [];
}

function reloadHistoryInputs(){
    var container = $(".historyInputs");
    var content = container.innerHTML;
    container.innerHTML= content; 
    
   //this line is to watch the result in console , you can remove it later	
    console.log("Refreshed"); 
}

// update history input in cookie
function updateHistInput(updatedSet) {
    let cookieValue = "histInput=" + JSON.stringify(Array.from(updatedSet));
    document.cookie = cookieValue + "; expires=Sat, 01 Jan 2050 00:00:00 GMT";
    console.log("Here are the cookies:\n" + document.cookie);
}

// Add the input to the cookie
function addToCookie(input) {
    console.log("add cookie " + input);
    let inputSet = getHisInputFromCookie();
    inputSet.add(input);
    updateHisInput(inputSet);
}

// Remove a history input
function removeInputFromCookie(input) {
    console.log("delete cookie " + input);
    let inputSet = getHisInputFromCookie();
    inputSet.delete(input);
    updateHisInput(inputSet);
}

// Get set of history input from the cookie
function getHistInputFromCookie() {
    let cookieValue = document.cookie
        .split('; ')
        .find(cookie => cookie.startsWith('histInput='));
    
    if (cookieValue) {
        let inputArray = JSON.parse(cookieValue.split('=')[1]);
        return new Set(inputArray);
    } else {
        return new Set();
    }
}

function storeDataInLocalStorage(input, data) {
    if (data.sortedPages.length === 0) return;
    localStorage.setItem(input, JSON.stringify(data));
    console.log("Data stored in local storage");
}

function getDataFromLocalStorage(input) {
    const data = localStorage.getItem(input);
    if (data) {
        return JSON.parse(data);
    } else {
        return null;
    }
}

function createChecklistForLocalStorageKeys(ORonlyFlag = false) {
    // Get the checklist element using jQuery
    const checklist = $("#localStorageKeys .historyInputs");
    const checklistForAND = $("#localStorageKeys .ANDInputs");

    // Clear the current checklist
    checklist.empty();
    if (!ORonlyFlag) {
        checklistForAND.empty();
    }

    // Retrieve all keys from local storage
    const keys = Object.keys(localStorage);

    // Create and append checkboxes for each key
    keys.forEach(key => {
        const checkbox = $("<input type='checkbox'>").attr("name", key);
        const span = $("<span></span>").text(key);
        const label = $("<label></label>").append(checkbox, span);
        const div = $("<div></div>").append(label);
        const button = $("<button></button>").html("&#10006;").addClass("removeHitorybutton").css({
            "top": "50%",
          }).click(function() {
            localStorage.removeItem(key);
          });
        div.addClass("cat").append(button);
        checkbox.prop("checked", false);
        checklist.append(div);

        if (!ORonlyFlag) {
            checklistForAND.append(div.clone().addClass('glow'));
        }
    });

    const colorMap = new Map();
    const defaultColor = `rgb(${128}, ${128}, ${128})`;
    colorMap.set("defualt", defaultColor);
    selectedInputSet = new Set();

    // Add event listener to handle checkbox change event
    checklist.on("change", "input[type='checkbox']", function() {
        const isChecked = $(this).is(":checked");
        const key = $(this).attr("name");
        resetSelectedPageID();
        selectedPageID = new Set(basePageIDSet);
        if (isChecked) {
            // Add the selected item to the set
            selectedInputSet.add(key);
            let backgroundColor;
            do {
                backgroundColor = getRandomColor();
            } while (colorMap.has(backgroundColor));
            $(this).parent().parent().css("background-color", backgroundColor);
            colorMap.set(key, backgroundColor);
        } else {
            // Remove the unselected item from the set
            selectedInputSet.delete(key);
            colorMap.delete(key);
            $(this).parent().parent().css("background-color", defaultColor);
        }
        // Print all the elements in the set
        console.log(selectedInputSet);
        const myArray = Array.from(selectedInputSet);
        const mergedList = [...baseMergedList];
        myArray.forEach(element => {  
            const data = getDataFromLocalStorage(element);
            for (var j = 0; j < data.sortedPages.length; j++) {
                const index = data.sortedPages[j];
                const page = data.pages[index];
                mergedList.push([index, element, page.similarity, colorMap.get(element)]);
                selectedPageID.add(index);
            }
        });   
        selectedPageIDList = Array.from(selectedPageID);
        mergedList.sort((a, b) => b[2] - a[2]);
        console.log(mergedList);
        console.log(basePageIDSet);
        console.log(selectedPageIDList);
        updateMergedUI(mergedList);
    });

    if (!ORonlyFlag) {
        checklistForAND.on("change", "input[type='checkbox']", function() {
            $(this).parent().parent().addClass('glowBG');
            $(this).prop("disabled", true);
            const key = $(this).attr("name");
            let pageIDFilter = JSON.stringify(selectedPageIDList);
            getPages(key, pageIDFilter, selectedPageIDList.length, true); 
            addORToSearchSequence();
            addBrackets();
            addANDToSearchSequence(key);
            displaySearchSequence();
        })
    }
}

function getRandomColor() {
    const min = 2;
    const max = 23;
    const red = Math.floor(Math.random() * 10 * (max - min + 1) + min);
    const green = Math.floor(Math.random() * 10 * (max - min + 1) + min);
    const blue = Math.floor(Math.random() * 10 * (max - min + 1) + min);
    return `rgb(${red}, ${green}, ${blue})`;
}