          
class SynapseProperties:

    internalDelay = 0   # default from NetworkML.xsd
    preDelay = 0        # default from NetworkML.xsd
    postDelay = 0       # default from NetworkML.xsd
    propDelay = 0       # default from NetworkML.xsd
    weight = 1          # default from NetworkML.xsd
    threshold = 0       # default from NetworkML.xsd
    
    def __str__(self):
        return ("SynapseProperties: internalDelay: %s, preDelay: %s, postDelay: %s, propDelay: %s, weight: %s, threshold: %s" \
                    % (self.internalDelay, self.preDelay, self.postDelay, self.propDelay, self.weight, self.threshold))
                    
    def copy(self):
        sp = SynapseProperties()
        sp.internalDelay = self.internalDelay
        sp.preDelay = self.preDelay
        sp.postDelay = self.postDelay
        sp.propDelay = self.propDelay
        sp.weight = self.weight
        sp.threshold = self.threshold
        
        return sp