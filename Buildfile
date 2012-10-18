repositories.remote << 'http://repo1.maven.org/maven2'

JAVAHELP = artifact('javax.help:javahelp:jar:2.0.05')
MRJADAPTER = artifact('net.roydesign:mrjadapter:jar:1.1').from('libs/MRJAdapter.jar')
COLORPICKER = artifact('com.bric:colorpicker:jar:NA').from('libs/colorpicker.jar')
FONTCHOOSER = artifact('com.connectina:fontchooser:jar:NA').from('libs/fontchooser.jar')

define 'logisim' do
  project.version = '2.7.2'
  compile.using :lint=>'all'
  compile.with JAVAHELP, MRJADAPTER, COLORPICKER, FONTCHOOSER
  compile {
    puts "Including dependencies into generated jar..."
    Dir.glob('libs/*.jar') do |item|
        unzip( 'target/classes' => item ).exclude('META-INF/*').extract
    end
  }
  manifest['Main-Class'] = 'com.cburch.logisim.Main'
  package(:jar).include _('target/resources'), :as=>'resources'
end