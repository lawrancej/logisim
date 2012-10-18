repositories.remote << 'http://repo1.maven.org/maven2' << 'http://nexus.gephi.org/nexus/content/repositories/public'

JAVAHELP = artifact('javax.help:javahelp:jar:2.0.05')
MRJADAPTER = download artifact('net.roydesign:mrjadapter:jar:1.1') => 'http://www.docjar.com/jar/MRJAdapter.jar'
COLORPICKER = download artifact('com.bric:colorpicker:jar:1.0') => 'http://javagraphics.java.net/jars/ColorPicker.jar'
FONTCHOOSER = artifact('com.connectina.swing:fontchooser:jar:1.0')
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
  package(:jar)
end