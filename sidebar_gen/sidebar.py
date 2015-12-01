def processLines(sidebarLines,firstFlag = False):
	if len(sidebarLines) == 0:
		print '</ul>'
		print '</li>'
	elif sidebarLines[0][0] != " ":
		if (not firstFlag):
			print '</ul>'
			print '</li>'
		print '<li class="devsite-nav-item devsite-nav-item-section-expandable">'
		print '<a class="devsite-nav-title devsite-nav-title-no-path " tabindex="0">'
		print '<span>{}</span></a>'.format(sidebarLines[0])
		print '<a class="devsite-nav-toggle material-icons devsite-nav-toggle-collapsed"></a>'
		print '<ul class="devsite-nav-section devsite-nav-section-collapsed">'
		processLines(sidebarLines[1:])
	else:
		link = sidebarLines[0].split()[0]
		text = ' '.join(sidebarLines[0].split()[1:])
		print '<li class="devsite-nav-item"><a href="{}" class="devsite-nav-title gc-analytics-event">'.format(link)
		print '<span>{}</span></a></li>'.format(text)
		processLines(sidebarLines[1:])

def buildSidebar(sidebarLines):
	print '<script>'
	print '{% include nav.js %}'
	print '</script>'
	print '<nav class="devsite-section-nav-responsive devsite-nav" tabindex="0" style="left: -256px;">'
	print '<ul class="devsite-nav-expandable">'
	processLines(sidebarLines,True)
	print '</ul>'
	print '</nav>'
	print '<div class="devsite-main-content clearfix" style="margin-top: 40px;">'
	print '<nav class="devsite-section-nav devsite-nav" style="left: auto; max-height: 737px; position: relative; top: 0px;">'
	print '<ul class="devsite-nav-expandable">'
	processLines(sidebarLines,True)
	print '</ul>'
	print '</nav>'
	print '<nav class="devsite-page-nav devsite-nav" style="position: relative; left: auto; max-height: 737px; top: 0px;"></nav>'

def removeBlankLines(ls):
	return filter(lambda x: x, ls)

if __name__ == "__main__":
	import sys
	sidebarLocation = '../_includes/docs_sidebar.html'
	oldStdout = sys.stdout
	sys.stdout = open(sidebarLocation, 'w')
	sidebarLines = open("sidebar.txt").readlines()
	sidebarLines = removeBlankLines(sidebarLines)
	buildSidebar(sidebarLines)
	sys.stdout = oldStdout
	print "New sidebar written to {}".format(sidebarLocation)
