import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import Navbar from '../components/Navbar';

const features = [
  {
    icon: '⚡',
    title: 'Real-time Collaboration',
    description: 'Code together with your team in real-time with live cursor tracking.',
  },
  {
    icon: '🎯',
    title: 'Multi-language Support',
    description: 'Support for 50+ programming languages with syntax highlighting.',
  },
  {
    icon: '🚀',
    title: 'Instant Execution',
    description: 'Run your code instantly and see results in real-time.',
  },
  {
    icon: '💬',
    title: 'Inline Comments',
    description: 'Add comments and discussions directly in your code.',
  },
];

const container = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
    },
  },
};

const item = {
  hidden: { y: 20, opacity: 0 },
  show: { y: 0, opacity: 1 },
};

export default function Landing() {
  return (
    <div className="min-h-screen bg-surface-dark grid-bg">
      <Navbar />
      
      <section className="relative pt-32 pb-20 px-6 overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-b from-transparent via-primary-500/5 to-transparent pointer-events-none" />
        
        <motion.div
          initial="hidden"
          animate="show"
          variants={container}
          className="max-w-6xl mx-auto"
        >
          <motion.div variants={item} className="text-center mb-16">
            <h1 className="text-6xl md:text-8xl font-bold mb-6">
              <span className="gradient-text">Code</span>
              <span className="text-white">Sync</span>
            </h1>
            <p className="text-xl md:text-2xl text-zinc-400 max-w-2xl mx-auto">
              The next-generation collaborative code editor. Create, code, and collaborate in real-time with your team from anywhere.
            </p>
            <div className="flex items-center justify-center gap-4 mt-10">
              <Link
                to="/register"
                className="px-8 py-4 bg-gradient-to-r from-accent-cyan to-accent-magenta text-surface-dark font-semibold rounded-xl hover:opacity-90 transition-all duration-300 glow-effect"
              >
                Get Started Free
              </Link>
              <Link
                to="/login"
                className="px-8 py-4 border border-surface-border text-white font-semibold rounded-xl hover:bg-surface-hover transition-all duration-300"
              >
                Sign In
              </Link>
            </div>
          </motion.div>

          <motion.div
            variants={item}
            className="mt-20 relative"
          >
            <div className="glass-card rounded-2xl p-2 glow-effect">
              <div className="rounded-xl overflow-hidden bg-surface-darker">
                <div className="flex items-center gap-2 px-4 py-3 border-b border-surface-border">
                  <div className="w-3 h-3 rounded-full bg-accent-rose" />
                  <div className="w-3 h-3 rounded-full bg-accent-amber" />
                  <div className="w-3 h-3 rounded-full bg-primary-400" />
                </div>
                <div className="p-6 font-mono text-sm text-zinc-300">
                  <div><span className="text-accent-magenta">const</span> <span className="text-accent-cyan">collaborate</span> = <span className="text-accent-amber">true</span>;</div>
                  <div><span className="text-accent-magenta">const</span> <span className="text-accent-cyan">create</span> = <span className="text-accent-amber">true</span>;</div>
                  <div><span className="text-accent-magenta">const</span> <span className="text-accent-cyan">code</span> = <span className="text-accent-amber">async</span> () ={'>'} {'{'}</div>
                  <div className="pl-4"><span className="text-accent-rose">await</span> sync(<span className="text-accent-amber">team</span>);</div>
                  <div className="pl-4"><span className="text-accent-rose">return</span> <span className="text-accent-amber">innovation</span>;</div>
                  <div>{'}'};</div>
                </div>
              </div>
            </div>
          </motion.div>
        </motion.div>
      </section>

      <section className="py-20 px-6">
        <div className="max-w-6xl mx-auto">
          <motion.div
            initial="hidden"
            whileInView="show"
            viewport={{ once: true }}
            variants={container}
            className="grid md:grid-cols-2 lg:grid-cols-4 gap-6"
          >
            {features.map((feature, index) => (
              <motion.div
                key={index}
                variants={item}
                className="glass-card rounded-xl p-6 hover:border-accent-cyan/30 transition-colors duration-300"
              >
                <div className="text-4xl mb-4">{feature.icon}</div>
                <h3 className="text-xl font-semibold text-white mb-2">{feature.title}</h3>
                <p className="text-zinc-400">{feature.description}</p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      <footer className="py-10 px-6 border-t border-surface-border">
        <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <span className="gradient-text font-bold text-xl">CodeSync</span>
          </div>
          <p className="text-zinc-500 text-sm">© 2024 CodeSync. Built with ❤️ for developers.</p>
        </div>
      </footer>
    </div>
  );
}