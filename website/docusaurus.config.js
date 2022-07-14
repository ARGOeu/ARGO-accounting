module.exports = {
  title: 'Accounting System API Documentation',
  tagline: 'Learn how the Accounting System API works',
  url: 'https://argoeu.github.io',
  baseUrl: '/ARGO-accounting/',
  onBrokenLinks: 'throw',
  favicon: 'img/favicon.ico',
  organizationName: 'ARGOeu', // Usually your GitHub org/user name.
  projectName: 'ARGO-accounting', // Usually your repo name.
  themeConfig: {
    navbar: {
      title: 'Accounting System API',
      logo: {
        alt: 'grnet logo',
        src: 'img/grnet-logo.png',
      },
      items: [
        {
          to: 'docs/',
          activeBasePath: 'docs',
          label: 'Docs',
          position: 'left',
        },
        {
          href: 'https://github.com/ARGOeu/ARGO-accounting',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      logo: {
        alt: 'GRNET',
        src: 'img/grnet-logo.png',
        href: 'http://www.grnet.gr/',
      },
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Explore Documentation',
              to: 'docs/',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'Github',
              href: 'https://github.com/ARGOeu/ARGO-accounting',
            }
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/ARGOeu/ARGO-accounting',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} <a href="http://www.grnet.gr/"> GRNET </a>`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          // It is recommended to set document id as docs home page (`docs/` path).
          sidebarPath: require.resolve('./sidebars.js'),
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};