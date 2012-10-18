repositories.remote << 'http://repo1.maven.org/maven2'

JAVAHELP = artifact('javax.help:javahelp:jar:2.0.05')
MRJADAPTER = artifact('net.roydesign:mrjadapter:jar:1.1').from('libs/MRJAdapter.jar')
COLORPICKER = artifact('com.bric:colorpicker:jar:NA').from('libs/colorpicker.jar')
FONTCHOOSER = artifact('com.connectina:fontchooser:jar:NA').from('libs/fontchooser.jar')
DEPENDENCIES = [JAVAHELP, MRJADAPTER, COLORPICKER, FONTCHOOSER]

# Extract all jars into target/classes, excluding META-INF/*
def extractJars(jars)
    jars.each do |item|
        unzip( 'target/classes' => item.to_s ).exclude('META-INF/*').extract
    end
end

define 'logisim' do
  project.version = '2.7.2'
  compile.using :lint=>'all'
  compile.with DEPENDENCIES
  compile {
    puts "Including dependencies into generated jar..."
    extractJars(DEPENDENCIES)
  }
  manifest['Main-Class'] = 'com.cburch.logisim.Main'
  package(:jar).include _('target/resources'), :as=>'resources'
end