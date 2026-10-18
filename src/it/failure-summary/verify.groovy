// the tree, as in the other scenarios, and then what the reporter prints when the run ends
new GroovyShell(binding).evaluate(new File(itSourceDir, 'verify-tree.groovy'))
new GroovyShell(binding).evaluate(new File(itSourceDir, 'verify-summary.groovy'))
