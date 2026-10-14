// the forks complete their classes in any order
binding.setVariable('sortClassBlocks', true)
new GroovyShell(binding).evaluate(new File(itSourceDir, 'verify-tree.groovy'))
