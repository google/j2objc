function saveCollapseState() {
  var menus = document.querySelectorAll('.devsite-nav-item-section-expandable');
  var expandedMenus = [];
  var f = function (menu, index) {
    if (menu.children[1].className.indexOf('devsite-nav-toggle-collapsed') == -1) {
      expandedMenus.push(index.toString());
    }
  };
  Array.prototype.forEach.call(menus, f);
  document.cookie = "nav_state=" + expandedMenus.join(',');
}

function getCollapseState() {
  var cname = "nav_state=";
  var cookies = document.cookie.split(';');
  for (var i = 0; i < cookies.length; i++) {
    var c = cookies[i].trim();
    if (c.indexOf(cname) == 0) {
      return c.substring(cname.length, c.length).split(',');
    }
  }
  return [];
}

// Expands both children of devsite-nav-item-section-expandable.
function expandMenu(menu){
  menu.children[1].className = menu.children[1].className.replace( /(?:^|\s)devsite-nav-toggle-collapsed(?!\S)/g , ' devsite-nav-toggle-expanded' )
  menu.children[2].className = menu.children[2].className.replace( /(?:^|\s)devsite-nav-section-collapsed(?!\S)/g , ' devsite-nav-section-expanded' )
}

document.onreadystatechange = function () {
  switch (document.readyState) {
    case 'complete':
      // Rebuild page navigation links.
      var menus = document.querySelectorAll('.devsite-nav-item-section-expandable');
      var nav_state = getCollapseState();
      console.log("Cookie");
      console.log(nav_state);

      var f = function (menu, index) {
        // TODO(tball): fix menu collapsing.
        //if (nav_state.indexOf(index.toString()) != -1){
          expandMenu(menu);
        //}
        menu.addEventListener('click', function() {
          saveCollapseState();
        });
      };
      Array.prototype.forEach.call(menus, f);

      // Mark current page in navigation.
      var path = "{{page.url}}";
      var nav_links = document.querySelectorAll('.devsite-nav-item');
      var active_page_class = "devsite-nav-active";
      console.log(path);
      f = function(link, index){
        if (link.classList.length != 1){
          return;
        }
        var link_path = link.children[0].href
        if(link_path.substring(link_path.length - path.length) == path) {
          console.log(link.children[0].href);
          // Expand element by getting the devsite-nav-item-section-expandable.
          expandMenu(link.parentNode.parentNode)
          link.className = link.className + " " + active_page_class;
        }
      };
      Array.prototype.forEach.call(nav_links, f);

      break;
  }
};
