require 'simple_uuid'

def template(id, content)
%{<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ExchangePackage Type="Request" Id="#{id}" Action="SmevRequest">
    <Content>
#{content}
    </Content>
</ExchangePackage>}
end

Dir.glob("./samples/*.xml").each do |sample|
  request = template(SimpleUUID::UUID.new.to_guid, File.read(sample))
  path = File.join("samples/prepared", File.basename(sample))
  File.write(path, request)
  puts path
  `docker run -it --rm --network=host -v "$(pwd)/samples:/samples" edenhill/kcat:1.7.1 -P -b host.docker.internal:9092 -t smev_requests -p 0 /#{path}`
end

