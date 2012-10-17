repositories.remote << 'http://repo1.maven.org/maven2'

JAVAHELP = 'javax.help:javahelp:jar:2.0.05'

define 'logisim' do
  project.version = '2.7.2'
  compile.using :lint=>'all'
  compile.with JAVAHELP, 'libs/*.jar'
  compile {
    puts "Including dependencies into generated jar..."
    Dir.glob('libs/*.jar') do |item|
        unzip( 'target/classes' => item ).exclude('META-INF/*').extract
    end
  }
  manifest['Main-Class'] = 'com.cburch.logisim.Main'
  package(:jar).include _('target/resources'), :as=>'resources'
end