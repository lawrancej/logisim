require 'rubygems' # Necessary for Ruby < 1.9
require 'nokogiri' # Don't forget to do this first: gem install nokogiri
require 'fileutils'

def www(base_href)
    www_src = 'www'
    www_target = 'target'
    FileUtils.mkdir_p("#{www_target}/#{www_src}")
    FileUtils.cp_r www_src, www_target
    Dir.glob("#{www_src}/*/*.html") do |page|
        next if page.include? 'template.html'
        destination = "#{www_target}/#{page}"

        # Load template, language menu, and document
        template = Nokogiri::HTML(open("#{File.dirname(page)}/template.html"))
        menu = Nokogiri::HTML(open("#{www_src}/langmenu.html"))
        doc = Nokogiri::HTML(open(page))

        # Replace template head with document head, and add in base href
        template.at_xpath('//head') << doc.xpath('//head').children()
        base_node = Nokogiri::XML::Node.new "base", template
        base_node['href'] = base_href
        template.at_xpath('//head') << base_node

        # Replace langmenu, contents
        template.at_xpath('//langmenu').replace(menu.xpath('//body').children())
        template.at_xpath('//contents').replace(doc.xpath('//body').children())

        # Write file
        FileUtils.mkdir_p(File.dirname(destination))
        open(destination,'w') { |f| template.write_xml_to f}
    end
    FileUtils.mv Dir.glob("#{www_target}/#{www_src}/root/*"), "#{www_target}/#{www_src}"
end

www(FileUtils.pwd + "/target/www/")