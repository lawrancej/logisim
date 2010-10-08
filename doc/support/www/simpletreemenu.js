// I've edited this to remove the idea of "persistence" through cookies,
// and also to format the code closer to the Java style that I prefer. -CBB

var ddtreemenu = new Object();

ddtreemenu.closefolder = "closed.gif"; //set image path to "closed" folder image
ddtreemenu.openfolder = "open.gif"; //set image path to "open" folder image

ddtreemenu.setPath = function(relpath) {
	ddtreemenu.closefolder = relpath + '/closed.gif';
	ddtreemenu.openfolder = relpath + '/open.gif';
}

//////////No need to edit beyond here///////////////////////////

ddtreemenu.createTree = function(treeid){
	var ultags = document.getElementById(treeid).getElementsByTagName("ul");
	for (var i = 0; i < ultags.length; i++) {
		ddtreemenu.buildSubTree(treeid, ultags[i]);
	}
}

ddtreemenu.buildSubTree = function(treeid, ulelement) {
	ulelement.parentNode.className = "submenu";
	if (ulelement.getAttribute("rel") == null || ulelement.getAttribute("rel") == false) {
		// UL has NO rel attribute explicted added by user
		ulelement.setAttribute("rel", "closed");
		ulelement.style.display = "none";
	} else if (ulelement.getAttribute("rel") == "open") {
		// this UL has an explicit rel value of "open":
		// expand this UL plus all parent ULs (so the most inner UL is revealed!)
		ddtreemenu.expandSubTree(treeid, ulelement);
	}
	ulelement.parentNode.onclick = function(e) {
		var submenu = this.getElementsByTagName("ul")[0];
		if (submenu.getAttribute("rel") == "closed"){
			submenu.style.display = "block";
			submenu.setAttribute("rel", "open");
			ulelement.parentNode.style.backgroundImage = "url(" + ddtreemenu.openfolder + ")";
		} else if (submenu.getAttribute("rel") == "open"){
			submenu.style.display = "none";
			submenu.setAttribute("rel", "closed");
			ulelement.parentNode.style.backgroundImage = "url("+ddtreemenu.closefolder+")";
		}
		ddtreemenu.preventpropagate(e);
	};
	ulelement.onclick = function(e) {
		ddtreemenu.preventpropagate(e);
	};
}

ddtreemenu.expandSubTree=function(treeid, ulelement) { //expand a UL element and any of its parent ULs
	var rootnode = document.getElementById(treeid);
	var currentnode = ulelement;
	currentnode.style.display = "block";
	currentnode.parentNode.style.backgroundImage = "url("+ddtreemenu.openfolder+")";
	while (currentnode != rootnode) {
		if (currentnode.tagName == "ul") { //if parent node is a UL, expand it too
			currentnode.style.display = "block";
			currentnode.setAttribute("rel", "open"); //indicate it's open
			currentnode.parentNode.style.backgroundImage = "url(" + ddtreemenu.openfolder + ")";
		}
		currentnode=currentnode.parentNode;
	}
}

ddtreemenu.flatten=function(treeid, action){ //expand or contract all UL elements
var ultags=document.getElementById(treeid).getElementsByTagName("ul")
for (var i=0; i<ultags.length; i++){
ultags[i].style.display=(action=="expand")? "block" : "none"
var relvalue=(action=="expand")? "open" : "closed"
ultags[i].setAttribute("rel", relvalue)
ultags[i].parentNode.style.backgroundImage=(action=="expand")? "url("+ddtreemenu.openfolder+")" : "url("+ddtreemenu.closefolder+")"
}
}

////A few utility functions below//////////////////////

ddtreemenu.preventpropagate=function(e){ //prevent action from bubbling upwards
if (typeof e!="undefined")
e.stopPropagation()
else
event.cancelBubble=true
}
