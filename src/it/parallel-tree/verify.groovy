// the classes run in parallel, so they complete in any order
binding.setVariable('sortClassBlocks', true)
new GroovyShell(binding).evaluate(new File(itSourceDir, 'verify-tree.groovy'))
